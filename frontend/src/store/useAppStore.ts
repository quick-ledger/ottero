import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AppState {
    selectedCompanyId: string | null;
    selectedCompanyName: string | null;
    actions: {
        setSelectedCompany: (id: string, name: string) => void;
        clearSelectedCompany: () => void;
    };
}

export const useAppStore = create<AppState>()(
    persist(
        (set) => ({
            selectedCompanyId: null,
            selectedCompanyName: null,
            actions: {
                setSelectedCompany: (id, name) => set({ selectedCompanyId: id, selectedCompanyName: name }),
                clearSelectedCompany: () => set({ selectedCompanyId: null, selectedCompanyName: null }),
            },
        }),
        {
            name: 'app-storage', // name of the item in the storage (must be unique)
            partialize: (state) => ({
                selectedCompanyId: state.selectedCompanyId,
                selectedCompanyName: state.selectedCompanyName,
            }),
        }
    )
);

export const useSelectedCompanyId = () => useAppStore((state) => state.selectedCompanyId);
export const useAppActions = () => useAppStore((state) => state.actions);
