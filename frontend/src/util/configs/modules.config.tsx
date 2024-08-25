import React, { FunctionComponent } from 'react'

import { DebtModule } from '../../route-modules/Debt.module.tsx'
import { MapModule } from '../../route-modules/Map.module'
import { RiddleModule } from '../../route-modules/Riddle.module'
import { TaskModule } from '../../route-modules/Task.module'
import { NewsModule } from '../../route-modules/News.module'
import { ProfileModule } from '../../route-modules/Profile.module'
import { CommunitiesModule } from '../../route-modules/Communities.module'
import { TokenModule } from '../../route-modules/Token.module'
import { ImpressumModule } from '../../route-modules/Impressum.module'
import { EventsModule } from '../../route-modules/Events.module'
import { LeaderBoardModule } from '../../route-modules/LeaderBoard.module'

import { ExtraPageModule } from '../../route-modules/ExtraPage.module'
import { HomeModule } from '../../route-modules/Home.module'
import { FormModule } from '../../route-modules/Form.module'
import { TeamModule } from '../../route-modules/Team.module'
import { RaceModule } from '../../route-modules/Race.module'
import { QRFightModule } from '../../route-modules/QRFight.module'
import { AccessKeyModule } from '../../route-modules/AccessKey.module'

export enum AvailableModules {
  HOME = 'HOME',
  RIDDLE = 'RIDDLE',
  TASK = 'TASK',
  IMPRESSUM = 'IMPRESSUM',
  NEWS = 'NEWS',
  PROFILE = 'PROFILE',
  COMMUNITIES = 'COMMUNITIES',
  TOKEN = 'TOKEN',
  EVENTS = 'EVENTS',
  EXTRA = 'EXTRA',
  FORM = 'FORM',
  LEADER_BOARD = 'LEADER_BOARD',
  TEAM = 'TEAM',
  RACE = 'RACE',
  QR_FIGHT = 'QR_FIGHT',
  ACCESS_KEY = 'ACCESS_KEY',
  MAP = 'MAP',
  DEBT = 'DEBT'
}

export const RoutesForModules: Record<AvailableModules, FunctionComponent> = {
  [AvailableModules.HOME]: HomeModule,
  [AvailableModules.RIDDLE]: RiddleModule,
  [AvailableModules.TASK]: TaskModule,
  [AvailableModules.IMPRESSUM]: ImpressumModule,
  [AvailableModules.NEWS]: NewsModule,
  [AvailableModules.PROFILE]: ProfileModule,
  [AvailableModules.COMMUNITIES]: CommunitiesModule,
  [AvailableModules.TOKEN]: TokenModule,
  [AvailableModules.EVENTS]: EventsModule,
  [AvailableModules.EXTRA]: ExtraPageModule,
  [AvailableModules.FORM]: FormModule,
  [AvailableModules.LEADER_BOARD]: LeaderBoardModule,
  [AvailableModules.RACE]: RaceModule,
  [AvailableModules.QR_FIGHT]: QRFightModule,
  [AvailableModules.TEAM]: TeamModule,
  [AvailableModules.ACCESS_KEY]: AccessKeyModule,
  [AvailableModules.MAP]: MapModule,
  [AvailableModules.DEBT]: DebtModule
}

export function GetRoutesForModules(modules: AvailableModules[]) {
  return modules.map((m) => <React.Fragment key={m.valueOf()}>{RoutesForModules[m]([])}</React.Fragment>)
}

export const EnabledModules: AvailableModules[] = [
  AvailableModules.HOME,
  AvailableModules.PROFILE,
  AvailableModules.RIDDLE,
  AvailableModules.COMMUNITIES,
  AvailableModules.TOKEN,
  AvailableModules.IMPRESSUM,
  AvailableModules.TASK,
  AvailableModules.EVENTS,
  AvailableModules.NEWS,
  AvailableModules.EXTRA,
  AvailableModules.FORM,
  AvailableModules.LEADER_BOARD,
  AvailableModules.RACE,
  AvailableModules.QR_FIGHT,
  AvailableModules.TEAM,
  AvailableModules.ACCESS_KEY,
  AvailableModules.MAP,
  AvailableModules.DEBT
]
