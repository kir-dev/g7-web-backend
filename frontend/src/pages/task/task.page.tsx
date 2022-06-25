import { Alert, AlertIcon, Box, Button, FormLabel, Heading, Image, Stack, Text, Textarea, useToast } from '@chakra-ui/react'
import { chakra } from '@chakra-ui/system'
import axios from 'axios'
import { useRef, useState, lazy } from 'react'
import { Helmet } from 'react-helmet'
import { Navigate, useNavigate, useParams } from 'react-router-dom'
import { Controller, SubmitHandler, useForm, useWatch } from 'react-hook-form'
import { taskFormat, TaskFormatDescriptor, taskStatus, taskType } from '../../util/views/task.view'
import { FilePicker } from './components/FilePicker'
import { Loading } from '../../common-components/Loading'
import { CmschPage } from '../../common-components/layout/CmschPage'
import { CustomBreadcrumb } from '../../common-components/CustomBreadcrumb'
import { Paragraph } from '../../common-components/Paragraph'
import { TaskStatusBadge } from './components/TaskStatusBadge'
import { stringifyTimeStamp } from '../../util/core-functions.util'
import { TaskDetailsSkeleton } from './components/taskDetailsSkeleton'
import { useTaskFullDetailsQuery } from '../../api/hooks/useTaskFullDetailsQuery'
import { LinkButton } from '../../common-components/LinkButton'
import Markdown from '../../common-components/Markdown'
import { CustomForm } from './components/CustomForm'
import { useTaskSubmissionMutation } from '../../api/hooks/useTaskSubmissionMutation'
const CodeEditor = lazy(() => import('./components/CodeEditor'))

export interface FormInput {
  textAnswer?: string
  fileAnswer?: File
  customForm?: ({
    value: string | number
  } & TaskFormatDescriptor)[]
}

const TaskPage = () => {
  const [fileAnswer, setFileAnswer] = useState<File | undefined>(undefined)
  const filePickerRef = useRef<FilePicker>(null)

  const toast = useToast()
  const { id } = useParams()
  const navigate = useNavigate()
  const { setValue, handleSubmit, control } = useForm<FormInput>()
  const customResults = useWatch({ control, name: 'customForm' })

  if (!id) return <Navigate to="/" replace />

  const taskDetailsQuery = useTaskFullDetailsQuery(id, () => {
    navigate('/bucketlist')
    toast({
      title: 'Challange nem található',
      description: 'Ilyen challange nem létezik vagy nincs jogosultságod hozzá.',
      status: 'error',
      isClosable: true
    })
  })
  const taskSubmissionMutation = useTaskSubmissionMutation()

  if (taskDetailsQuery.isSuccess) {
    const taskDetails = taskDetailsQuery.data
    const textAllowed = taskDetails.task?.type === taskType.TEXT || taskDetails.task?.type === taskType.BOTH
    const fileAllowed =
      taskDetails.task?.type === taskType.IMAGE || taskDetails.task?.type === taskType.BOTH || taskDetails.task?.type === taskType.ONLY_PDF
    const submissionAllowed = taskDetails?.status === taskStatus.NOT_SUBMITTED || taskDetails?.status === taskStatus.REJECTED
    const reviewed = taskDetails.status === taskStatus.ACCEPTED || taskDetails.status === taskStatus.REJECTED

    const onSubmit: SubmitHandler<FormInput> = async (data) => {
      if ((!fileAllowed || fileAnswer) && submissionAllowed) {
        const formData = new FormData()
        formData.append('taskId', id)
        if (fileAnswer) {
          if (fileAnswer.size > 31457280) {
            toast({
              title: 'Túl nagy kép',
              description: 'A feltöltött kép túllépte a 30 MB-os feltöltési korlátot!',
              status: 'error',
              isClosable: true
            })
            return
          }
          formData.append('file', fileAnswer)
        }
        if (textAllowed) {
          switch (taskDetails.task?.format) {
            case taskFormat.TEXT:
              if (data.textAnswer) {
                formData.append('textAnswer', data.textAnswer)
              } else {
                toast({
                  title: 'Üres megoldás',
                  description: 'Üres megoldást nem küldhetsz be.',
                  status: 'error',
                  isClosable: true
                })
                return
              }
              break
            case taskFormat.FORM:
              if (customResults) {
                formData.append(
                  'textAnswer',
                  customResults.reduce((acc, current) => acc + current.title + ': ' + current.value.toString() + current.suffix, '')
                )
              }
              break
          }
        }

        taskSubmissionMutation.mutate(formData, {
          onSuccess: (result) => {
            if (result.status === 'OK') {
              toast({
                title: 'Megoldás elküldve',
                status: 'success',
                isClosable: true
              })
              setValue('textAnswer', '')
              if (filePickerRef.current) {
                filePickerRef.current.reset()
              }
              taskDetailsQuery.refetch()
              window.scrollTo(0, 0)
            } else {
              toast({
                title: result.status,
                status: 'error',
                isClosable: true
              })
            }
          },
          onError: (error) => {
            toast({
              title: error.message || 'Hiba a megoldása elküldése közben',
              status: 'error',
              isClosable: true
            })
          }
        })
      } else {
        toast({
          title: 'Üres megoldás',
          description: 'Üres megoldást nem küldhetsz be.',
          status: 'error',
          isClosable: true
        })
      }
    }

    let textInput = null
    if (textAllowed && taskDetails.task) {
      switch (taskDetails.task.format) {
        case taskFormat.TEXT:
          textInput = (
            <Box mt={5}>
              <FormLabel htmlFor="textAnswer">Szöveges válasz</FormLabel>
              <Controller
                name="textAnswer"
                control={control}
                render={({ field }) => <Textarea id="textAnswer" placeholder="Szöveges válasz" {...field} />}
              />
            </Box>
          )
          break
        case taskFormat.FORM:
          textInput = <CustomForm formatDescriptor={taskDetails.task.formatDescriptor} control={control} />
          break
        case taskFormat.CODE:
          textInput = <CodeEditor />
          break
        default:
          textInput = null
      }
    }
    const fileInput = fileAllowed && (
      <Box>
        <FormLabel>Csatolt fájl (max. méret: 30 MB)</FormLabel>
        <FilePicker
          onFileChange={(fileArray) => setFileAnswer(fileArray[0])}
          placeholder="Csatolt fájl"
          clearButtonLabel="Törlés"
          accept={taskDetails.task?.type === taskType.ONLY_PDF ? '.pdf' : 'image/jpeg,image/png,image/jpg,image/gif'}
          ref={filePickerRef}
        />
      </Box>
    )

    const breadcrumbItems = [
      {
        title: 'Bucketlist',
        to: '/bucketlist'
      },
      {
        title: taskDetails.task?.categoryName,
        to: `/bucketlist/kategoria/${taskDetails.task?.categoryId}`
      },
      {
        title: taskDetails.task?.title
      }
    ]

    return (
      <CmschPage loginRequired groupRequired>
        <Helmet title={taskDetails.task?.title} />
        <CustomBreadcrumb items={breadcrumbItems} />
        <Heading mb={5}>{taskDetails.task?.title}</Heading>
        <TaskStatusBadge status={taskDetails.status} fontSize="lg" />
        <Box mt={5}>
          <Markdown text={taskDetails.task?.description} />
        </Box>
        {taskDetails.task?.expectedResultDescription && (
          <Text size="sm" mt={5}>
            <chakra.span fontWeight="bold">Beadandó formátum:</chakra.span>
            &nbsp;{taskDetails.task?.expectedResultDescription}
          </Text>
        )}
        {taskDetails.status !== taskStatus.NOT_SUBMITTED && (
          <>
            <Heading size="md" mt={8}>
              Beküldött megoldás
            </Heading>
            {textAllowed && taskDetails.submission && <Paragraph mt={2}>{taskDetails.submission.textAnswer}</Paragraph>}
            {fileAllowed && taskDetails.submission && (
              <Box>
                {taskDetails.submission.imageUrlAnswer && taskDetails.submission.imageUrlAnswer.length > 'task/'.length && (
                  <Image
                    src={`${process.env.REACT_APP_API_BASE_URL}/cdn/${taskDetails.submission.imageUrlAnswer}`}
                    alt="Beküldött megoldás"
                  />
                )}
                {taskDetails.submission.fileUrlAnswer && taskDetails.submission.fileUrlAnswer.length > 'task/'.length && (
                  <LinkButton
                    href={`${process.env.REACT_APP_API_BASE_URL}/cdn/${taskDetails.submission.fileUrlAnswer}`}
                    external
                    colorScheme="brand"
                    mt={5}
                  >
                    Letöltés
                  </LinkButton>
                )}
              </Box>
            )}
          </>
        )}
        {reviewed && taskDetails.submission && (
          <>
            <Heading size="md" mt={8}>
              Értékelés
            </Heading>
            <Text mt={2}>Javító üzenete: {taskDetails.submission.response}</Text>
            <Text>Pont: {taskDetails.submission.score} pont</Text>
          </>
        )}

        {submissionAllowed && (
          <>
            <Heading size="md" mt={8}>
              {taskDetails.status === taskStatus.REJECTED ? 'Újra beküldés' : 'Beküldés'}
            </Heading>
            <Stack mt={5}>
              <form onSubmit={handleSubmit(onSubmit)}>
                <Alert variant="left-accent" status="info">
                  <AlertIcon />A feladat beadási határideje: {stringifyTimeStamp(taskDetails.task?.availableTo || 0)}
                </Alert>
                {textInput}
                {fileInput}
                <Box>
                  <Button mt={3} colorScheme="brand" type="submit">
                    Küldés
                  </Button>
                </Box>
              </form>
            </Stack>
          </>
        )}
      </CmschPage>
    )
  } else {
    return (
      <Loading>
        <TaskDetailsSkeleton />
      </Loading>
    )
  }
}

export default TaskPage
