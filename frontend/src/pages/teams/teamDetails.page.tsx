import { Navigate, useParams } from 'react-router-dom'

import { useTeamDetails } from '../../api/hooks/team/queries/useTeamDetails'
import { TeamDetailsCore } from './components/teamDetailsCore'
import { AbsolutePaths } from '../../util/paths'

export default function TeamDetailsPage() {
  const { id } = useParams()
  const { data: team, isLoading, error } = useTeamDetails(id)
  if (team?.ownTeam) return <Navigate to={AbsolutePaths.MY_TEAM} />
  return <TeamDetailsCore team={team} isLoading={isLoading} error={error?.message} />
}
