import { Box, ComponentDefaultProps, VStack } from '@chakra-ui/react'
import { useNavigate } from 'react-router-dom'
import { Menu } from '../../../api/contexts/config/types'
import { SidebarContent } from './helpers/SidebarContent'
import { SidebarFooter } from './helpers/SidebarFooter'
import { SidebarHeading } from './helpers/SidebarHeading'

export const SidebarFixed = (props: ComponentDefaultProps) => {
  const navigate = useNavigate()
  const loginMenu: Menu = {
    name: 'Bejelentkezés',
    url: '/login',
    external: false,
    children: []
  }
  const onLoginClick = () => {
    navigate(loginMenu.url)
  }

  return (
    <VStack width="20rem" alignItems="stretch" p={5} {...props} borderRight="1px solid #ededed">
      <Box>
        <SidebarHeading />
      </Box>
      <Box flex={1}>
        <SidebarContent onClose={() => {}} />
      </Box>
      <Box pb={{ base: 6, md: 12 }}>
        <SidebarFooter loginMenu={loginMenu} onLoginClick={onLoginClick} />
      </Box>
    </VStack>
  )
}
