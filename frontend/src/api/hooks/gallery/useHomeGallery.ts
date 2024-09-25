import { useQuery } from 'react-query'
import { QueryKeys } from '../queryKeys.ts'
import axios from 'axios'
import { ApiPaths } from '../../../util/paths.ts'
import { GalleryView } from '../../../util/views/gallery.view.ts'

export const useHomeGallery = (onError?: (err: any) => void) => {
  return useQuery<GalleryView, Error>(
    QueryKeys.HOME_GALLERY,
    async () => {
      const response = await axios.get<GalleryView>(ApiPaths.HOME_GALLERY)
      return response.data
    },
    { onError: onError }
  )
}
