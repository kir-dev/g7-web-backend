import { Divider, Heading, Text } from '@chakra-ui/react'
import { Helmet } from 'react-helmet-async'
import { useConfigContext } from '../../api/contexts/config/ConfigContext.tsx'
import { useDebtQuery } from '../../api/hooks/debt/useDebtQuery.ts'
import { useProfileQuery } from '../../api/hooks/profile/useProfileQuery.ts'
import { ComponentUnavailable } from '../../common-components/ComponentUnavailable.tsx'
import { CmschPage } from '../../common-components/layout/CmschPage.tsx'
import Markdown from '../../common-components/Markdown.tsx'
import { PageStatus } from '../../common-components/PageStatus.tsx'
import { ProfileQR } from '../profile/components/ProfileQR.tsx'
import { DebtListItem } from './components/debt-list-item.tsx'

const DebtPage = () => {
  const debtComponent = useConfigContext()?.components.debt
  const profileComponent = useConfigContext()?.components.profile
  const profileQuery = useProfileQuery()
  const { data, isError, isLoading } = useDebtQuery()

  if (!debtComponent) return <ComponentUnavailable />

  const title = debtComponent.title || 'Tartozások'

  if (isError || isLoading || !data) return <PageStatus isLoading={isLoading} isError={isError} title={title} />

  return (
    <CmschPage>
      <Helmet title={title} />
      <Heading as="h1" variant="main-title">
        {title}
      </Heading>
      {profileQuery.data && (
        <>
          <Divider my={8} borderWidth={2} />
          <ProfileQR profile={profileQuery.data} component={profileComponent} />
          <Divider my={8} borderWidth={2} />
        </>
      )}
      {debtComponent.topMessage && <Markdown text={debtComponent.topMessage} />}
      {data.debts.map((item) => (
        <DebtListItem key={item.product + item.price} item={item} />
      ))}
      {data.debts.length === 0 && (
        <Text align="center" mt={10} fontWeight="bold">
          Nincs tranzakciód eddig, szaladj és vegyél valami zurát!
        </Text>
      )}
    </CmschPage>
  )
}

export default DebtPage
