import axios from 'axios'
import { useQuery } from 'react-query'
import { ApiPaths } from '../../../util/paths'
import { DebtDto } from '../../../util/views/debt.view.ts'
import { QueryKeys } from '../queryKeys'

export const useDebtQuery = () => {
  return useQuery<DebtDto, Error>(
    QueryKeys.DEBTS,
    async () => {
      const response = await axios.get<DebtDto>(ApiPaths.DEBTS)
      return response.data
    },
    {
      refetchOnMount: 'always'
    }
  )
}
