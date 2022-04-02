import React, { createContext, useEffect, useState } from 'react'
import Cookies from 'js-cookie'
import axios from 'axios'
import { API_BASE_URL } from './configurations'
import { ProfileDTO } from '../types/dto/profile'
import { useServiceContext } from './useServiceContext'
import { useNavigate } from 'react-router-dom'

const CookieKeys = {
  JWT_TOKEN: 'CMSCH_JWT_TOKEN'
}

export type AuthContextType = {
  isLoggedIn: boolean
  profile: ProfileDTO | undefined
  onLoginSuccess: (response: any) => void
  onLoginFailure: (response: any) => void
  onLogout: (response: any) => void
  updateProfile: () => void
}

export const AuthContext = createContext<AuthContextType>({
  isLoggedIn: false,
  profile: undefined,
  onLoginSuccess: () => {},
  onLoginFailure: () => {},
  onLogout: () => {},
  updateProfile: () => {}
})

export const AuthProvider: React.FC = ({ children }) => {
  const { throwError } = useServiceContext()
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(typeof Cookies.get(CookieKeys.JWT_TOKEN) !== 'undefined')
  const [profile, setProfile] = useState<ProfileDTO>()
  const navigate = useNavigate()

  const onLoginSuccess = async (response: any) => {
    console.log('[DEBUG] onLoginSuccess, response:', response)
    return // todo: remove this return
    const { accessToken } = response

    const res = await axios.post<{ profile: ProfileDTO; jwtToken: string }>(`${API_BASE_URL}/api/login`, { accessToken })
    const { jwtToken } = res.data

    Cookies.set(CookieKeys.JWT_TOKEN, jwtToken, { expires: 2 })
    setIsLoggedIn(true)
    navigate('/profil')
  }

  const onLoginFailure = (response: any) => {
    console.error('[ERROR] onLoginFailure, response:', response)
    throwError('Hiba az AuthSCH-val való bejelentkeztetés során!', { toast: true, toastStatus: 'warning', toHomePage: true })
  }

  const onLogout = () => {
    Cookies.remove(CookieKeys.JWT_TOKEN)
    setIsLoggedIn(false)
    navigate('/')
  }

  const updateProfile = () => {
    if (isLoggedIn)
      axios
        .get<ProfileDTO>(`${API_BASE_URL}/api/profile`)
        .then((res) => {
          if (typeof res.data !== 'object') onLogout()
          setProfile(res.data)
        })
        .catch(() => {
          throwError('Újra be kell jelentkezned!', { toast: true, toastStatus: 'warning', toHomePage: true })
          onLogout()
        })
  }

  useEffect(() => {
    updateProfile()
  }, [isLoggedIn])

  return (
    <AuthContext.Provider value={{ isLoggedIn, profile, onLoginSuccess, onLoginFailure, onLogout, updateProfile }}>
      {children}
    </AuthContext.Provider>
  )
}
