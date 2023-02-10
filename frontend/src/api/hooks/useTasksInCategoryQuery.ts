import axios from 'axios'
import { useQuery } from 'react-query'
import { TaskCategoryFullDetails } from '../../util/views/task.view'
import { QueryKeys } from './queryKeys'
import { joinPath } from '../../util/core-functions.util'
import { ApiPaths } from '../../util/paths'

export const useTasksInCategoryQuery = (categoryId: string | undefined, onError: (err: any) => void) => {
  return useQuery<TaskCategoryFullDetails, Error, TaskCategoryFullDetails>(
    [QueryKeys.TASKS_IN_CATEGORY, categoryId],
    async () => {
      const response = await axios.get<TaskCategoryFullDetails>(joinPath(ApiPaths.TASK_CATEGORY, categoryId))
      return response.data
    },
    { onError }
  )
}
