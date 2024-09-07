import { RoleTypeString } from '../../../util/views/profile.view'

export interface ConfigDto {
  role: RoleTypeString
  menu: Menu[]
  components: Components
}

export interface Menu {
  name: string
  url: string
  external: boolean
  children: Menu[]
}

export interface Components {
  app: App
  style: Style
  userHandling: UserHandling
  countdown: Countdown
  debt: Debt
  event: Event
  extraPage: ExtraPage
  groupselection: GroupSelection
  home: Home
  gallery: Gallery
  impressum: Impressum
  leaderboard: Leaderboard
  location: Location
  login: Login
  news: News
  profile: Profile
  race: Race
  riddle: Riddle
  form: Signup
  task: Task
  team: Team
  token: Token
  pushnotification: PushNotification
  qrFight: QrFight
  communities: Communities
  footer: Footer
}

export interface App {
  defaultComponent: string
  siteName: string
  warningLevel: string
  warningMessage: string
}

export interface Footer {
  devAlt: string
  devLogo: string
  devWebsiteUrl: string
  facebookUrl: string
  footerMessage: string
  hostAlt: string
  hostLogo: string
  hostWebsiteUrl: string
  instagramUrl: string
  minimalisticFooter: boolean
  partnerAlts: string
  partnerLogoUrls: string
  partnerTitle: string
  partnerWebsiteUrls: string
  schdesignEnabled: boolean
  schonherzEnabled: boolean
  vikEnabled: boolean
  bmeEnabled: true
  sponsorAlts: string
  sponsorLogoUrls: string
  sponsorTitle: string
  sponsorWebsiteUrls: string
  sponsorsEnabled: boolean
}

export interface Countdown {
  title: string
  enabled: boolean
  showOnly?: boolean
  topMessage: string
  timeToCountTo: number
  keepOnAfterCountdownOver: boolean
  imageUrl: string
  blurredImage: boolean
}

export interface GroupSelection {
  selectionEnabled: boolean
}

export interface Event {
  title: string
  seekToCurrentCurrent: boolean
  separateDays: boolean
  topMessage: string
  enableDetailedView: boolean
  filterByCategory: boolean
  filterByLocation: boolean
  filterByDay: boolean
  searchEnabled: boolean
}

export interface Home {
  title: string
  welcomeMessage: string
  content: string
  youtubeVideoId: string
  showEvents: boolean
  showNews: boolean
  showGalleryImages: boolean
}

export interface Gallery {
  title: string
}

export interface News {
  title: string
  showDetails: boolean
}

export interface Race {
  title: string
  visible: boolean
  defaultCategoryDescription: string
  freestyleCategoryName: string
  extraCategoriesVisible: boolean
  searchEnabled: boolean
}

export interface Riddle {
  title: string
  skipEnabled: boolean
  skipAfterGroupsSolved: number
}

export interface Impressum {
  title: string
  topMessage: string
  developerSchamiUrl: string
  developerBalintUrl: string
  developerLaciUrl: string
  developerBeniUrl: string
  developerTriszUrl: string
  developerSamuUrl: string
  developerDaniUrl: string
  developerMateUrl: string
  developersBottomMessage: string
  leadOrganizers: Organizer[] // JSONified Organizer[]
  leadOrganizersMessage: string
  otherOrganizers: Organizer[] // JSONified Organizer[]
  otherOrganizersMessage: string
}

export interface Organizer {
  name: string
  roles: string
  avatar: string
}

export interface Leaderboard {
  leaderboardEnabled: string
  leaderboardDetailsEnabled: boolean
  leaderboardDetailsByCategoryEnabled: boolean
  leaderboardFrozen: string
  maxGroupEntryToShow: number
  maxUserEntryToShow: number
  minScoreToShow: number
  showGroupBoard: boolean
  showScores: boolean
  showGroupOfUser: boolean
  showUserBoard: boolean
  title: string
  searchEnabled: boolean
}

export interface Profile {
  aliasChangeEnabled: boolean
  title: string
  showTasks: boolean
  taskCounterName: string
  showRiddles: boolean
  riddleCounterName: string
  showTokens: boolean
  tokenCounterName: string
  showFullName: boolean
  showGuild: boolean
  showMajor: boolean
  showAlias: boolean
  showGroup: boolean
  showNeptun: boolean
  showQr: boolean
  qrTitle: string
  showEmail: boolean
  showProfilePicture: boolean
  groupTitle: string
  groupLeadersHeader: string
  messageBoxContent: string
  messageBoxLevel: string
  showGroupName: boolean
  showGroupLeadersLocations: boolean
  showMinimumToken: boolean
  minTokenMsg: string
  minTokenAchievedMsg: string
  profileIncomplete: string
  showIncompleteProfile: boolean
  profileComplete: string
}

export interface Style {
  lightBackgroundColor: string
  lightContainerColor: string
  lightTextColor: string
  lightBrandingColor: string
  lightBackgroundUrl: string
  lightMobileBackgroundUrl: string
  lightLogoUrl: string
  darkModeEnabled: boolean
  deviceTheme: boolean
  forceDarkMode: boolean
  darkBackgroundColor: string
  darkContainerColor: string
  darkTextColor: string
  darkBackgroundUrl: string
  darkMobileBackgroundUrl: string
  darkLogoUrl: string
  mainFontName: string
  mainFontWeight: number
  displayFontName: string
  displayFontWeight: number
  lightFooterTransparent: boolean
  lightNavbarTransparent: boolean
  darkFooterTransparent: boolean
  darkNavbarTransparent: boolean
}

export interface Task {
  title: string
  profileRequiredTitle: string
  profileRequiredMessage: string
  regularTitle: string
  regularMessage: string
  resubmissionEnabled: boolean
}

export interface Token {
  title: string
  collectFeature: boolean
  collectType: string
  collectRequired: string
  minTokenMsg: string
  minTokenAchievedMsg: string
  defaultIcon: string
  defaultTestIcon: string
}

export interface Debt {}

export interface UserHandling {}

export interface ExtraPage {}

export interface Location {
  topMessage: string
  bottomMessage: string
}

export interface Login {
  authschPromoted: boolean
  bottomMessage: string
  googleSsoEnabled: boolean
  keycloakAuthName: string
  keycloakEnabled: boolean
  onlyBmeProvider: boolean
  title: string
}

export interface Team {
  myTitle: string
  adminTitle: string
  createTitle: string
  teamEditTitle: string
  teamEditTopMessage: string
  creationEnabled: boolean
  teamEditEnabled: boolean
  teamLogoUploadEnabled: boolean
  joinEnabled: boolean
  kickEnabled: boolean
  promoteLeadershipEnabled: boolean
  racesByDefault: boolean
  leaderNotes: string
  selectableByDefault: boolean
  showAdvertisedForms: boolean
  showRaceButton: boolean
  showTasks: boolean
  showTeamDetails: boolean
  showTeamMembersPublicly: boolean
  showTeamScore: boolean
  showTeamScoreDetailsButton: boolean
  teamCreationTopMessage: string
  title: string
  togglePermissionEnabled: boolean
}

export interface PushNotification {
  notificationsEnabled: boolean
  permissionPromptText: string
  permissionAcceptText: string
  permissionDenyText: string
  permissionAllowNeverShowAgain: boolean
}

export interface QrFight {
  title: string
  enabled: boolean
  topMessage: string
}

export interface Signup {
  langTooEarly: string
  langTooLate: string
  langNotEnabled: string
  langFull: string
  langNotFound: string
  langSubmitted: string
  langRejected: string
  langAccepted: string
  langGroupInsufficient: string
  langNoSubmission?: string
}

export interface Communities {
  title: string
}
