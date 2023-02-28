import { Badge, Box, Flex, Heading, Text, useColorModeValue, VStack } from '@chakra-ui/react'
import { Helmet } from 'react-helmet-async'
import { Link, Navigate, useParams } from 'react-router-dom'
import { CustomBreadcrumb } from '../../common-components/CustomBreadcrumb'
import { CmschPage } from '../../common-components/layout/CmschPage'
import { TaskStatusBadge } from './components/TaskStatusBadge'
import { useTasksInCategoryQuery } from '../../api/hooks/task/useTasksInCategoryQuery'
import { useConfigContext } from '../../api/contexts/config/ConfigContext'
import { AbsolutePaths } from '../../util/paths'
import { ComponentUnavailable } from '../../common-components/ComponentUnavailable'
import { PageStatus } from '../../common-components/PageStatus'

const TaskCategoryPage = () => {
  const { id } = useParams()
  const bg = useColorModeValue('brand.100', 'brand.500')
  const hoverBg = useColorModeValue('brand.200', 'brand.400')
  const { isLoading, isError, data } = useTasksInCategoryQuery(id || 'UNKNOWN')

  const component = useConfigContext()?.components.task

  if (!id) return <Navigate to={AbsolutePaths.TASKS} />

  if (!component) return <ComponentUnavailable />

  if (isError || isLoading || !data) return <PageStatus isLoading={isLoading} isError={isError} title={component.title} />

  const breadcrumbItems = [
    {
      title: component?.title || 'Feladatok',
      to: AbsolutePaths.TASKS
    },
    {
      title: data.categoryName
    }
  ]

  return (
    <CmschPage loginRequired>
      <Helmet title={data.categoryName} />
      <CustomBreadcrumb items={breadcrumbItems} />
      <Heading>{data.categoryName}</Heading>
      {data.tasks && data.tasks.length > 0 ? (
        <VStack spacing={4} mt={5} align="stretch">
          {data.tasks.map((task) => (
            <Box key={task.task.id} bg={bg} px={6} py={2} borderRadius="md" _hover={{ bgColor: hoverBg }}>
              <Link to={`${AbsolutePaths.TASKS}/${task.task.id}`}>
                <Flex align="center" justifyContent="space-between">
                  <Flex align="center">
                    <Text fontWeight="bold" fontSize="xl">
                      {task.task.title}
                    </Text>
                    {task.task.availableTo < new Date().valueOf() / 1000 && (
                      <Badge ml={5} variant="solid" colorScheme="red" fontSize="sm">
                        LEJÁRT
                      </Badge>
                    )}
                  </Flex>
                  <TaskStatusBadge status={task.status} fontSize="sm" />
                </Flex>
              </Link>
            </Box>
          ))}
        </VStack>
      ) : (
        <Text>Nincs egyetlen feladat sem ebben a kategóriában.</Text>
      )}
    </CmschPage>
  )
}

export default TaskCategoryPage
