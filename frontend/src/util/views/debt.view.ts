export interface DebtView {
  product: string
  price: number
  sellerName: string
  representativeName: string
  payed: boolean
  shipped: boolean
  icon: string
}

export interface DebtDto {
  debts: DebtView[]
}
