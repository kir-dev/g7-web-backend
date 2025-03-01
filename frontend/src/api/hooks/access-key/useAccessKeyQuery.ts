import { useQuery } from '@tanstack/react-query'
import { AccessKey } from '../../../util/views/accessKey'
import { QueryKeys } from '../queryKeys'
import axios from 'axios'
import { ApiPaths } from '../../../util/paths'

export const useAccessKey = () => {
  return useQuery<AccessKey, Error>({
    retry: false,
    queryKey: [QueryKeys.ACCESS_KEY],
    queryFn: async () => {
      const response = await axios.get<AccessKey>(ApiPaths.ACCESS_KEY)
      return response.data
    }
  })
}
