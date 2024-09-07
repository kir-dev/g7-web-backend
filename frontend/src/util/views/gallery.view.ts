export type GalleryView = {
  photos: GalleryItemView[]
}

export type GalleryItemView = {
  title: string
  highlighted: boolean
  showOnHomePage: boolean
  url: string
}
