import { lazy } from 'react'
import { Route } from 'react-router-dom'
import { Paths } from '../util/paths.ts'

const DebtPage = lazy(() => import('../pages/debt/debt.page'))

export function DebtModule() {
  return (
    <Route path={Paths.DEBT}>
      <Route index element={<DebtPage />} />
    </Route>
  )
}
