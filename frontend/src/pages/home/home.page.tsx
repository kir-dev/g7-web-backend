import { Box, Divider, Grid, Heading, Text, useColorModeValue, VStack } from '@chakra-ui/react'
import { useMemo } from 'react'
import { Helmet } from 'react-helmet-async'
import { useConfigContext } from '../../api/contexts/config/ConfigContext'
import { useEventListQuery } from '../../api/hooks/event/useEventListQuery'
import { useHomeNews } from '../../api/hooks/home/useHomeNews'
import { ComponentUnavailable } from '../../common-components/ComponentUnavailable'

import { CmschPage } from '../../common-components/layout/CmschPage'
import { LinkButton } from '../../common-components/LinkButton'
import Markdown from '../../common-components/Markdown'
import { AbsolutePaths } from '../../util/paths'
import { NewsArticleView } from '../../util/views/news.view'
import Clock from '../countdown/components/clock'
import NewsListItem from '../news/components/NewsListItem'
import { EmbeddedVideo } from './components/EmbeddedVideo'
import { ImageCarousel } from './components/ImageCarousel.tsx'
import { Schedule } from './components/Schedule'
import { useHomeGallery } from '../../api/hooks/gallery/useHomeGallery.ts'

const HomePage = () => {
  const homeNews = useHomeNews()
  const eventList = useEventListQuery()
  const homeGallery = useHomeGallery()
  const config = useConfigContext()
  const countdownConfig = config?.components.countdown
  const homeConfig = config?.components.home

  const countTo = useMemo(() => {
    try {
      if (!countdownConfig) return new Date()
      return new Date(countdownConfig?.timeToCountTo * 1000)
    } catch (e) {
      return new Date()
    }
  }, [countdownConfig])

  const events = useMemo(() => {
    const timestampCorrectedEventList = eventList.data?.map((li) => {
      const { timestampStart, timestampEnd, ...rest } = li
      return { timestampStart: timestampStart * 1000, timestampEnd: timestampEnd * 1000, ...rest }
    })
    return timestampCorrectedEventList?.filter((li) => li.timestampStart > Date.now()).sort((a, b) => a.timestampStart - b.timestampStart)
  }, [eventList.data])

  if (!homeConfig) return <ComponentUnavailable />

  const eventsToday = events?.filter((ev) => isToday(ev.timestampStart)) || []
  const eventsLater = events?.filter((ev) => !isToday(ev.timestampStart)).slice(0, 3) || []

  return (
    <CmschPage>
      <Helmet />
      {homeConfig?.welcomeMessage && (
        <Heading variant="main-title" as="h1" size="3xl" textAlign="center" marginTop={10} lineHeight="1.2">
          {homeConfig?.welcomeMessage.split('{}')[0] + ' '}
          {homeConfig?.welcomeMessage.split('{}').length > 1 && (
            <>
              <Heading as="span" color={useColorModeValue('brand.500', 'brand.500')} size="3xl">
                {config?.components.app.siteName || 'CMSch'}
              </Heading>{' '}
              {homeConfig?.welcomeMessage.split('{}')[1]}
            </>
          )}
        </Heading>
      )}
      {homeConfig.showNews && homeNews.data && homeNews.data.length > 0 && (
        <>
          <Grid mt={10} templateColumns="1fr" gap={4}>
            {sortByHighlighted(homeNews.data).map((n: NewsArticleView) => (
              <NewsListItem news={n} fontSize="xl" useLink={config?.components?.news?.showDetails} key={n.title + n.timestamp} />
            ))}
          </Grid>
          <LinkButton colorScheme="brand" mt={5} href={AbsolutePaths.NEWS}>
            Összes hír
          </LinkButton>
        </>
      )}
      {countdownConfig?.enabled && (
        <>
          <Heading textAlign="center">{countdownConfig?.topMessage}</Heading>
          <Clock countTo={countTo} />
        </>
      )}

      {homeConfig?.youtubeVideoId && <EmbeddedVideo key={homeConfig?.youtubeVideoId} id={homeConfig?.youtubeVideoId} />}

      {homeConfig?.content && (
        <Box mt={10}>
          <Markdown text={homeConfig?.content} />
        </Box>
      )}

      {eventList.data && homeConfig?.showEvents && (
        <VStack>
          <Heading as="h2" size="lg" textAlign="center" mb={5} mt={20}>
            {config?.components?.event?.title}
          </Heading>
          <VStack spacing={10}>
            <Text textAlign="center" fontSize={25} fontWeight="bolder" marginTop={10}>
              Mai nap
            </Text>
            {eventsToday.length > 0 ? (
              <Schedule events={eventsToday} />
            ) : (
              <Text textAlign="center" color="gray.500" marginTop={10}>
                Nincs több esemény.
              </Text>
            )}
            <Divider />
            <Text textAlign="center" fontSize={25} fontWeight="bolder" marginTop={10}>
              Később
            </Text>
            {eventsLater.length > 0 ? (
              <Schedule verbose events={eventsLater} />
            ) : (
              <Text textAlign="center" color="gray.500" marginTop={10}>
                Nincs több esemény.
              </Text>
            )}
            <LinkButton colorScheme="brand" href={AbsolutePaths.EVENTS}>
              Részletek
            </LinkButton>
          </VStack>
        </VStack>
      )}

      {homeConfig.showGalleryImages && config.components.gallery && homeGallery.data && (
        <ImageCarousel images={homeGallery.data?.photos?.map((item) => item.url) ?? []} />
      )}
    </CmschPage>
  )
}

export default HomePage

const isToday = (timeStamp: number) => new Date(timeStamp).toDateString() === new Date().toDateString()

const sortByHighlighted = (news: NewsArticleView[]) => {
  return news.sort((a, b) => {
    if (a.highlighted && !b.highlighted) {
      return -1
    }
    if (b.highlighted && !a.highlighted) {
      return 1
    }
    return 0
  })
}
