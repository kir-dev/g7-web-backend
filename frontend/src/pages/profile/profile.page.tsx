import {
  Alert,
  AlertIcon,
  Box,
  Button,
  Center,
  CircularProgress,
  CircularProgressLabel,
  Divider,
  Flex,
  Heading,
  Link,
  Text,
  useColorModeValue,
  VStack
} from '@chakra-ui/react'
import { Helmet } from 'react-helmet-async'
import { Navigate, useNavigate } from 'react-router-dom'

import { useAuthContext } from '../../api/contexts/auth/useAuthContext'
import { CmschPage } from '../../common-components/layout/CmschPage'
import { LinkButton } from '../../common-components/LinkButton'
import { PresenceAlert } from '../../common-components/PresenceAlert'
import { API_BASE_URL } from '../../util/configs/environment.config'
import { GuildType, RoleType } from '../../util/views/profile.view'
import { completedPercent, submittedPercent } from './util/percentFunctions'
import { AbsolutePaths } from '../../util/paths'
import { useConfigContext } from '../../api/contexts/config/ConfigContext'
import templateStringReplace from '../../util/templateStringReplace'
import React, { useEffect } from 'react'
import { GroupLeaderContactList } from './components/GroupLeaderContactList'
import { ProfileQR } from './components/ProfileQR'
import Markdown from '../../common-components/Markdown'
import { PageStatus } from '../../common-components/PageStatus'
import { ComponentUnavailable } from '../../common-components/ComponentUnavailable'

const Map = React.lazy(() => import('../../common-components/map/GroupMapContainer'))

type Props = {}

const ProfilePage = ({}: Props) => {
  const { onLogout, profile, profileLoading, profileError, refetch } = useAuthContext()
  const navigate = useNavigate()

  useEffect(() => {
    const savedPath = localStorage.getItem('path')
    if (savedPath) {
      localStorage.removeItem('path')
      navigate(savedPath)
    }
    refetch()
  }, [])

  const component = useConfigContext()?.components.profile

  if (!component) return <ComponentUnavailable />
  if (profileError || profileLoading || !profile) return <PageStatus isLoading={profileLoading} isError={!!profileError} title="Profil" />

  if (!profile.loggedIn || profile.role === 'GUEST') {
    return <Navigate replace to="/" />
  }

  return (
    <CmschPage loginRequired>
      <Helmet title={component.title} />
      {component.messageBoxContent && (
        <Alert status="info" variant="left-accent" mt={5}>
          <AlertIcon />
          {component.messageBoxContent}
        </Alert>
      )}
      <PresenceAlert acquired={profile.collectedTokenCount} needed={profile.minTokenToComplete} mt={5} />

      {component?.showIncompleteProfile && (
        <Alert status={profile.profileIsComplete ? 'success' : 'error'} variant="left-accent" mb={5}>
          <AlertIcon />
          <Flex flexWrap="wrap" alignItems="center" w="full">
            <Box py={2}>
              {profile.profileIsComplete
                ? component.profileComplete
                : templateStringReplace(component.profileIncomplete, profile.incompleteTasks?.join(', '))}
            </Box>
            {!profile.profileIsComplete && (
              <Flex flex={1} justifyContent="end">
                <LinkButton href={AbsolutePaths.TASKS} ml={5} colorScheme="red">
                  Feladatok
                </LinkButton>
              </Flex>
            )}
          </Flex>
        </Alert>
      )}
      <Flex justifyContent="space-between" flexDirection={{ base: 'column', md: 'row' }}>
        <Box>
          {component.showFullName && <Heading>{profile.fullName}</Heading>}
          {component.showAlias && <Text fontSize="xl">Becenév: {profile.alias || 'nincs'}</Text>}
          {component.showNeptun && <Text fontSize="xl">Neptun: {profile.neptun || 'nincs'}</Text>}
          {component.showEmail && <Text fontSize="xl">E-mail: {profile.email || 'nincs'}</Text>}

          {component.showGuild && <Text fontSize="xl">Gárda: {GuildType[profile.guild] || 'nincs'}</Text>}
          {component.showMajor && <Text fontSize="xl">Szak: {profile.major || 'nincs'}</Text>}
          {component.showGroup && (
            <Text fontSize="xl">
              {component?.groupTitle}: {profile.groupName || 'nincs'}
            </Text>
          )}
        </Box>
        <VStack py={2} alignItems={{ base: 'flex-start', md: 'flex-end' }} mt={{ base: 5, md: 0 }}>
          {profile.role && RoleType[profile.role] >= RoleType.STAFF && (
            <LinkButton colorScheme="brand" href={`${API_BASE_URL}/admin/control`} external>
              Admin panel
            </LinkButton>
          )}
          {profile.groupSelectionAllowed && (
            <LinkButton colorScheme="brand" href={AbsolutePaths.CHANGE_GROUP}>
              {component?.groupTitle} módosítása
            </LinkButton>
          )}
          {component.aliasChangeEnabled && (
            <LinkButton colorScheme="brand" href={AbsolutePaths.CHANGE_ALIAS}>
              Becenév módosítása
            </LinkButton>
          )}
          <Button colorScheme="brand" variant="outline" onClick={onLogout}>
            Kijelentkezés
          </Button>
        </VStack>
      </Flex>
      {component.showGroup && <GroupLeaderContactList profile={profile} />}

      {profile.userMessage && (
        <>
          <Divider my={8} borderWidth={2} />
          <Markdown text={profile.userMessage} />
        </>
      )}

      {profile.groupMessage && (
        <>
          <Divider my={8} borderWidth={2} />
          <Markdown text={profile.groupMessage} />
        </>
      )}

      {component.showQr && profile.cmschId && (
        <>
          <Divider my={8} borderWidth={2} />
          <ProfileQR profile={profile} component={component} />
        </>
      )}
      {(component.showTasks || component.showRiddles || component.showTokens) && <Divider my={8} borderWidth={2} />}
      <Flex justify="center" alignItems="center" flexWrap="wrap">
        {component.showTasks && (
          <Center p={3}>
            <Flex direction="column" align="center">
              <Link
                href={AbsolutePaths.TASKS}
                fontSize="3xl"
                fontWeight={500}
                _hover={{
                  textDecoration: 'none',
                  color: useColorModeValue('brand.500', 'brand.600')
                }}
              >
                {component.taskCounterName}
              </Link>
              <Box>
                <CircularProgress
                  color={useColorModeValue('yellow.400', 'yellow.500')}
                  size="10rem"
                  position="absolute"
                  value={submittedPercent(profile) + completedPercent(profile)}
                  trackColor={useColorModeValue('gray.200', 'gray.500')}
                />
                <CircularProgress
                  color={useColorModeValue('green.500', 'green.600')}
                  size="10rem"
                  value={completedPercent(profile)}
                  trackColor="transparent"
                >
                  <CircularProgressLabel
                    color={
                      submittedPercent(profile) + completedPercent(profile) === 0 ? 'gray.500' : useColorModeValue('green.500', 'green.600')
                    }
                    pb={submittedPercent(profile) !== 0 ? '2.9rem' : '0'}
                  >
                    {Math.round(completedPercent(profile))}%
                  </CircularProgressLabel>
                  {submittedPercent(profile) !== 0 && (
                    <>
                      <CircularProgressLabel>
                        <Box
                          height="2px"
                          backgroundColor={useColorModeValue('gray.200', 'gray.700')}
                          width="70%"
                          mx="auto"
                          borderRadius="20%"
                        />
                      </CircularProgressLabel>
                      <CircularProgressLabel color={useColorModeValue('yellow.400', 'yellow.500')} pt="2.5rem">
                        {Math.round(submittedPercent(profile))}%
                      </CircularProgressLabel>
                    </>
                  )}
                </CircularProgress>
              </Box>
            </Flex>
          </Center>
        )}

        {component.showRiddles && (
          <Center p={3}>
            <Flex direction="column" align="center">
              <Link
                href={AbsolutePaths.RIDDLE}
                fontSize="3xl"
                fontWeight={500}
                _hover={{
                  textDecoration: 'none',
                  color: useColorModeValue('brand.500', 'brand.600')
                }}
              >
                {component.riddleCounterName}
              </Link>
              <CircularProgress
                color={useColorModeValue('green.500', 'green.600')}
                size="10rem"
                value={(profile.completedRiddleCount / profile.totalRiddleCount) * 100}
                trackColor={useColorModeValue('gray.200', 'gray.500')}
              >
                <CircularProgressLabel
                  color={profile.completedRiddleCount === 0 ? 'gray.500' : useColorModeValue('green.500', 'green.600')}
                >
                  {Math.round((profile.completedRiddleCount / profile.totalRiddleCount) * 100)}%
                </CircularProgressLabel>
              </CircularProgress>
            </Flex>
          </Center>
        )}
        {component.showTokens && (
          <Center p={3}>
            <Flex direction="column" align="center">
              <Link
                href={AbsolutePaths.TOKEN}
                fontSize="3xl"
                fontWeight={500}
                _hover={{
                  textDecoration: 'none',
                  color: useColorModeValue('brand.500', 'brand.600')
                }}
              >
                {component.tokenCounterName}
              </Link>
              <CircularProgress
                color={useColorModeValue('green.500', 'green.600')}
                size="10rem"
                value={(profile.collectedTokenCount / profile.totalTokenCount) * 100}
                trackColor={useColorModeValue('gray.200', 'gray.500')}
              >
                <CircularProgressLabel color={profile.collectedTokenCount === 0 ? 'gray.500' : useColorModeValue('green.500', 'green.600')}>
                  {Math.round((profile.collectedTokenCount / profile.totalTokenCount) * 100)}%
                </CircularProgressLabel>
              </CircularProgress>
            </Flex>
          </Center>
        )}
      </Flex>
      {component.showGroupLeadersLocations && <Map />}
    </CmschPage>
  )
}

export default ProfilePage
