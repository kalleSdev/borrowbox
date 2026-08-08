import { Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import Catalogue from './pages/Catalogue'
import Dashboard from './pages/Dashboard'
import ItemDetail from './pages/ItemDetail'
import Loans from './pages/Loans'
import Members from './pages/Members'
import NotFound from './pages/NotFound'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="catalogue" element={<Catalogue />} />
        <Route path="catalogue/:id" element={<ItemDetail />} />
        <Route path="members" element={<Members />} />
        <Route path="loans" element={<Loans />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}
