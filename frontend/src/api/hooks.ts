import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, query } from './client'
import type {
  ActivityEvent,
  Clock,
  Contract,
  CreateContractInput,
  CreateItemInput,
  Item,
  Member,
  MemberInput,
  UpdateItemInput,
} from './types'

export const keys = {
  members: ['members'] as const,
  member: (id: string) => ['members', id] as const,
  items: (filters: ItemFilters = {}) => ['items', filters] as const,
  item: (id: string) => ['items', id] as const,
  contracts: (memberId?: string) => ['contracts', memberId ?? 'all'] as const,
  clock: ['clock'] as const,
  events: ['events'] as const,
}

export interface ItemFilters {
  [param: string]: string | undefined
  name?: string
  maxPrice?: string
}

/**
 * Almost every write ripples further than the thing it changed: booking a loan
 * moves two members' credits and marks an item unavailable, and advancing the
 * day can start or end loans. Rather than track each ripple, a successful
 * write clears the lot.
 */
function useInvalidateEverything() {
  const client = useQueryClient()
  return () => client.invalidateQueries()
}

export function useMembers() {
  return useQuery({ queryKey: keys.members, queryFn: () => api.get<Member[]>('/members') })
}

export function useMember(id: string) {
  return useQuery({
    queryKey: keys.member(id),
    queryFn: () => api.get<Member>(`/members/${id}`),
    enabled: Boolean(id),
  })
}

export function useCreateMember() {
  const invalidate = useInvalidateEverything()
  return useMutation({
    mutationFn: (input: MemberInput) => api.post<Member>('/members', input),
    onSuccess: invalidate,
  })
}

export function useUpdateMember(id: string) {
  const invalidate = useInvalidateEverything()
  return useMutation({
    mutationFn: (input: MemberInput) => api.put<Member>(`/members/${id}`, input),
    onSuccess: invalidate,
  })
}

export function useDeleteMember() {
  const invalidate = useInvalidateEverything()
  return useMutation({
    mutationFn: (id: string) => api.del(`/members/${id}`),
    onSuccess: invalidate,
  })
}

export function useItems(filters: ItemFilters = {}) {
  return useQuery({
    queryKey: keys.items(filters),
    queryFn: () => api.get<Item[]>(`/items${query(filters)}`),
  })
}

export function useItem(id: string) {
  return useQuery({
    queryKey: keys.item(id),
    queryFn: () => api.get<Item>(`/items/${id}`),
    enabled: Boolean(id),
  })
}

export function useCreateItem() {
  const invalidate = useInvalidateEverything()
  return useMutation({
    mutationFn: (input: CreateItemInput) => api.post<Item>('/items', input),
    onSuccess: invalidate,
  })
}

export function useUpdateItem(id: string) {
  const invalidate = useInvalidateEverything()
  return useMutation({
    mutationFn: (input: UpdateItemInput) => api.put<Item>(`/items/${id}`, input),
    onSuccess: invalidate,
  })
}

export function useDeleteItem() {
  const invalidate = useInvalidateEverything()
  return useMutation({
    mutationFn: (id: string) => api.del(`/items/${id}`),
    onSuccess: invalidate,
  })
}

export function useContracts(memberId?: string) {
  return useQuery({
    queryKey: keys.contracts(memberId),
    queryFn: () => api.get<Contract[]>(`/contracts${query({ memberId })}`),
  })
}

export function useBookLoan() {
  const invalidate = useInvalidateEverything()
  return useMutation({
    mutationFn: (input: CreateContractInput) => api.post<Contract>('/contracts', input),
    onSuccess: invalidate,
  })
}

export function useClock() {
  return useQuery({ queryKey: keys.clock, queryFn: () => api.get<Clock>('/clock') })
}

export function useAdvanceDay() {
  const invalidate = useInvalidateEverything()
  return useMutation({
    mutationFn: () => api.post<Clock>('/clock/advance', {}),
    onSuccess: invalidate,
  })
}

export function useEvents(limit = 20) {
  return useQuery({
    queryKey: [...keys.events, limit],
    queryFn: () => api.get<ActivityEvent[]>(`/events${query({ limit })}`),
  })
}
