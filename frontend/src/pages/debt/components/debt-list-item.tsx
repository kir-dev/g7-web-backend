import { Box, HStack, Text, useColorModeValue } from '@chakra-ui/react'
import * as FaIcons from 'react-icons/fa'
import { FaCircleCheck, FaCircleXmark } from 'react-icons/fa6'
import { useOpaqueBackground } from '../../../util/core-functions.util.ts'
import { DebtView } from '../../../util/views/debt.view.ts'

interface DebtListItemProps {
  item: DebtView
}

export const DebtListItem = ({ item }: DebtListItemProps) => {
  const bg = useOpaqueBackground(1)

  const red = useColorModeValue('red.500', 'red.300')
  const green = useColorModeValue('green.500', 'green.300')

  return (
    <Box borderRadius="lg" padding={4} backgroundColor={bg} marginTop={5}>
      <HStack spacing={4} justify="space-between">
        <Text isTruncated display="flex" alignItems="center" gap={1}>
          <DebtIcon name="FaUser" />
          {item.product}
        </Text>
        <Text>{item.price}&nbsp;JMF</Text>
        {item.payed ? (
          <Text color={green} display="flex" alignItems="center" gap={1} fontWeight="bold">
            <FaCircleCheck /> Fizetve
          </Text>
        ) : (
          <Text color={red} display="flex" alignItems="center" gap={1} fontWeight="bold">
            <FaCircleXmark /> Fizetetlen
          </Text>
        )}
      </HStack>
    </Box>
  )
}

function DebtIcon({ name }: { name: string }) {
  const Icon = FaIcons[name as keyof typeof FaIcons] ?? null
  return <Icon />
}
