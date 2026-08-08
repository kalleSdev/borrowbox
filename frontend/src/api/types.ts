/**
 * The shapes the API returns. These mirror the response records on the Java
 * side; if one of those changes, this is the file that has to change with it.
 */

export type EventType =
  | 'MEMBER_JOINED'
  | 'ITEM_LISTED'
  | 'LOAN_AGREED'
  | 'LOAN_STARTED'
  | 'LOAN_ENDED'
  | 'DAY_ADVANCED'

export interface Member {
  id: string
  name: string
  email: string
  mobile: string
  credits: number
  joinedOnDay: number
  ownedItemCount: number
}

export interface Contract {
  itemId: string
  itemName: string
  lenderId: string
  lenderName: string
  borrowerId: string
  borrowerName: string
  startDay: number
  endDay: number
  durationInDays: number
  cost: number
}

export interface Item {
  id: string
  name: string
  description: string
  category: string
  costPerDay: number
  ownerId: string
  ownerName: string
  listedOnDay: number
  availableToday: boolean
  contracts: Contract[]
}

export interface ActivityEvent {
  day: number
  type: EventType
  description: string
}

export interface Clock {
  currentDay: number
  events: ActivityEvent[]
}

export interface MemberInput {
  name: string
  email: string
  mobile: string
}

export interface CreateItemInput {
  ownerId: string
  name: string
  description: string
  category: string
  costPerDay: number
}

export type UpdateItemInput = Omit<CreateItemInput, 'ownerId'>

export interface CreateContractInput {
  itemId: string
  borrowerId: string
  startDay: number
  endDay: number
}

/**
 * An RFC 7807 problem detail, which is what every failing request returns.
 * `errors` is only present on validation failures.
 */
export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  errors?: Record<string, string>
}
