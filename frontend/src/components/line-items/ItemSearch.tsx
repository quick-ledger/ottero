import { useState, useEffect, useRef, useMemo } from 'react';
import { createPortal } from 'react-dom';
import { useQuery } from '@tanstack/react-query';
import { useApi } from '@/hooks/useApi';
import { Input } from '@/components/ui/input';
import { Card } from '@/components/ui/card';
import { Loader2, Search, Package, Wrench, AlertTriangle } from 'lucide-react';
import { useAppStore } from '@/store/useAppStore';
import type { Product, Service, PaginatedResponse } from '@/types';

export interface SelectedItem {
    type: 'product' | 'service' | 'custom';
    id?: string;
    name: string;
    price: number;
    // Inventory info (products only)
    trackInventory?: boolean;
    quantityOnHand?: number;
    reorderPoint?: number;
}

interface ItemSearchProps {
    value: string;
    productItemId?: string | null;
    serviceItemId?: string | null;
    onSelect: (item: SelectedItem) => void;
    onCustomText: (text: string) => void;
    disabled?: boolean;
    placeholder?: string;
}

interface SearchResult {
    type: 'product' | 'service';
    id: string;
    name: string;
    description?: string;
    price: number;
    // Product-specific
    trackInventory?: boolean;
    quantityOnHand?: number;
    reorderPoint?: number;
}

// Cache duration: 5 minutes
const CACHE_TIME = 5 * 60 * 1000;

export const ItemSearch = ({
    value,
    productItemId,
    serviceItemId,
    onSelect,
    onCustomText,
    disabled,
    placeholder = "Search products/services or type custom..."
}: ItemSearchProps) => {
    const [searchTerm, setSearchTerm] = useState('');
    const [highlightedIndex, setHighlightedIndex] = useState(-1);
    const [, forceUpdate] = useState(0);
    const { selectedCompanyId } = useAppStore();
    const api = useApi();
    const inputRef = useRef<HTMLInputElement>(null);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const itemRefs = useRef<(HTMLDivElement | null)[]>([]);
    const isOpenRef = useRef(false);
    const blurTimeoutRef = useRef<NodeJS.Timeout | null>(null);

    // Helper to open/close dropdown with re-render
    const setIsOpen = (open: boolean) => {
        if (isOpenRef.current !== open) {
            isOpenRef.current = open;
            forceUpdate(n => n + 1);
        }
    };

    // When value changes externally (e.g., form reset), sync the input
    useEffect(() => {
        if (!isOpenRef.current) {
            setSearchTerm(value || '');
        }
    }, [value]);

    // Fetch ALL products once (cached for 5 minutes)
    const { data: productsData, isLoading: productsLoading } = useQuery({
        queryKey: ['products-cache', selectedCompanyId],
        queryFn: async () => {
            const { data } = await api.get<PaginatedResponse<Product>>(
                `/api/companies/${selectedCompanyId}/products`,
                { params: { page: 0, size: 500 } }
            );
            return data.content;
        },
        enabled: !!selectedCompanyId,
        staleTime: CACHE_TIME,
        gcTime: CACHE_TIME,
    });

    // Fetch ALL services once (cached for 5 minutes)
    const { data: servicesData, isLoading: servicesLoading } = useQuery({
        queryKey: ['services-cache', selectedCompanyId],
        queryFn: async () => {
            const { data } = await api.get<PaginatedResponse<Service>>(
                `/api/companies/${selectedCompanyId}/service_items`,
                { params: { page: 0, size: 500 } }
            );
            return data.content;
        },
        enabled: !!selectedCompanyId,
        staleTime: CACHE_TIME,
        gcTime: CACHE_TIME,
    });

    // Filter results locally based on search term
    const results: SearchResult[] = useMemo(() => {
        const term = searchTerm.toLowerCase().trim();
        if (term.length < 2) return [];

        const productResults = (productsData || [])
            .filter(p =>
                p.name.toLowerCase().includes(term) ||
                (p.description && p.description.toLowerCase().includes(term))
            )
            .slice(0, 10)
            .map((p): SearchResult => ({
                type: 'product',
                id: p.id,
                name: p.name,
                description: p.description,
                price: p.price,
                trackInventory: p.trackInventory,
                quantityOnHand: p.quantityOnHand,
                reorderPoint: p.reorderPoint,
            }));

        const serviceResults = (servicesData || [])
            .filter(s =>
                s.name.toLowerCase().includes(term) ||
                (s.description && s.description.toLowerCase().includes(term))
            )
            .slice(0, 10)
            .map((s): SearchResult => ({
                type: 'service',
                id: s.id,
                name: s.name,
                description: s.description,
                price: s.price,
            }));

        return [...productResults, ...serviceResults];
    }, [searchTerm, productsData, servicesData]);

    const isLoading = productsLoading || servicesLoading;
    const hasResults = results.length > 0;
    // Show dropdown when open and valid search term
    const showDropdown = isOpenRef.current && searchTerm.length >= 2;

    // Reset highlighted index when results change
    useEffect(() => {
        setHighlightedIndex(-1);
        itemRefs.current = [];
    }, [results.length, searchTerm]);

    // Calculate dropdown position for portal
    const [dropdownStyle, setDropdownStyle] = useState<React.CSSProperties>({});

    useEffect(() => {
        if (showDropdown && inputRef.current) {
            const rect = inputRef.current.getBoundingClientRect();
            setDropdownStyle({
                position: 'fixed',
                top: rect.bottom + 4,
                left: rect.left,
                width: rect.width,
                zIndex: 9999,
            });
        }
    }, [showDropdown, searchTerm]);

    // Handle item selection
    const handleSelect = (result: SearchResult) => {
        const item: SelectedItem = {
            type: result.type,
            id: result.id,
            name: result.name,
            price: result.price,
            trackInventory: result.trackInventory,
            quantityOnHand: result.quantityOnHand,
            reorderPoint: result.reorderPoint,
        };
        onSelect(item);
        setSearchTerm(result.name);
        setIsOpen(false);
    };

    // Handle using text as custom (on blur or explicit action)
    const handleBlur = () => {
        // Clear any previous timeout
        if (blurTimeoutRef.current) {
            clearTimeout(blurTimeoutRef.current);
        }
        // Longer delay to allow click on dropdown items
        blurTimeoutRef.current = setTimeout(() => {
            const stillFocused = document.activeElement === inputRef.current;
            // Only close if the input is no longer focused (user may have refocused)
            if (!stillFocused) {
                setIsOpen(false);
                // If no product/service is selected and we have text, treat as custom
                if (searchTerm && !productItemId && !serviceItemId) {
                    onCustomText(searchTerm);
                }
            }
            blurTimeoutRef.current = null;
        }, 300);
    };

    // Handle keyboard navigation
    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Escape') {
            setIsOpen(false);
            setHighlightedIndex(-1);
            inputRef.current?.blur();
            return;
        }

        if (e.key === 'ArrowDown') {
            e.preventDefault();
            if (!showDropdown) return;
            setHighlightedIndex(prev => {
                const next = prev < results.length - 1 ? prev + 1 : 0;
                // Scroll item into view
                setTimeout(() => {
                    itemRefs.current[next]?.scrollIntoView({ block: 'nearest' });
                }, 0);
                return next;
            });
            return;
        }

        if (e.key === 'ArrowUp') {
            e.preventDefault();
            if (!showDropdown) return;
            setHighlightedIndex(prev => {
                const next = prev > 0 ? prev - 1 : results.length - 1;
                // Scroll item into view
                setTimeout(() => {
                    itemRefs.current[next]?.scrollIntoView({ block: 'nearest' });
                }, 0);
                return next;
            });
            return;
        }

        if (e.key === 'Enter') {
            e.preventDefault();
            if (highlightedIndex >= 0 && highlightedIndex < results.length) {
                handleSelect(results[highlightedIndex]);
            } else if (!hasResults && searchTerm) {
                onCustomText(searchTerm);
                setIsOpen(false);
            }
            return;
        }
    };

    // Get linked item indicator
    const getLinkedIndicator = () => {
        if (productItemId) return <Package className="h-3 w-3 text-blue-500" />;
        if (serviceItemId) return <Wrench className="h-3 w-3 text-green-500" />;
        return null;
    };

    return (
        <div className="relative w-full">
            <div className="relative flex items-center">
                <Search className="absolute left-2 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground pointer-events-none" />
                <Input
                    ref={inputRef}
                    placeholder={placeholder}
                    value={searchTerm}
                    onChange={(e) => {
                        // Cancel any pending blur timeout since user is typing
                        if (blurTimeoutRef.current) {
                            clearTimeout(blurTimeoutRef.current);
                            blurTimeoutRef.current = null;
                        }
                        setSearchTerm(e.target.value);
                        setIsOpen(true);
                        // Clear linked IDs if user is typing
                        if (productItemId || serviceItemId) {
                            onCustomText(e.target.value);
                        }
                    }}
                    onFocus={() => {
                        // Cancel any pending blur timeout
                        if (blurTimeoutRef.current) {
                            clearTimeout(blurTimeoutRef.current);
                            blurTimeoutRef.current = null;
                        }
                        setIsOpen(true);
                    }}
                    onBlur={handleBlur}
                    onKeyDown={handleKeyDown}
                    disabled={disabled}
                    className="pl-7 pr-6"
                />
                {getLinkedIndicator() && (
                    <div className="absolute right-2 top-1/2 -translate-y-1/2">
                        {getLinkedIndicator()}
                    </div>
                )}
            </div>

            {showDropdown && createPortal(
                <Card
                    ref={dropdownRef}
                    style={dropdownStyle}
                    className="max-h-64 overflow-auto shadow-lg border bg-background"
                >
                    {isLoading ? (
                        <div className="p-3 flex items-center justify-center text-muted-foreground text-sm">
                            <Loader2 className="h-4 w-4 animate-spin mr-2" /> Loading...
                        </div>
                    ) : hasResults ? (
                        <div className="py-1">
                            {results.map((result, index) => (
                                <div
                                    key={`${result.type}-${result.id}`}
                                    ref={(el) => { itemRefs.current[index] = el; }}
                                    className={`px-3 py-2 cursor-pointer ${
                                        index === highlightedIndex ? 'bg-muted' : 'hover:bg-muted'
                                    }`}
                                    onMouseDown={(e) => e.preventDefault()}
                                    onMouseEnter={() => setHighlightedIndex(index)}
                                    onClick={() => handleSelect(result)}
                                >
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-2">
                                            {result.type === 'product' ? (
                                                <Package className="h-4 w-4 text-blue-500" />
                                            ) : (
                                                <Wrench className="h-4 w-4 text-green-500" />
                                            )}
                                            <span className="font-medium text-sm">{result.name}</span>
                                        </div>
                                        <span className="text-sm text-muted-foreground">
                                            ${result.price.toFixed(2)}
                                        </span>
                                    </div>
                                    {result.description && (
                                        <p className="text-xs text-muted-foreground ml-6 truncate">
                                            {result.description}
                                        </p>
                                    )}
                                    {result.type === 'product' && result.trackInventory && (
                                        <div className="ml-6 mt-1 flex items-center gap-1">
                                            {result.quantityOnHand === 0 ? (
                                                <span className="text-xs text-red-500 flex items-center gap-1">
                                                    <AlertTriangle className="h-3 w-3" /> Out of stock
                                                </span>
                                            ) : result.reorderPoint && result.quantityOnHand !== undefined &&
                                                result.quantityOnHand <= result.reorderPoint ? (
                                                <span className="text-xs text-amber-500 flex items-center gap-1">
                                                    <AlertTriangle className="h-3 w-3" /> {result.quantityOnHand} in stock (low)
                                                </span>
                                            ) : (
                                                <span className="text-xs text-muted-foreground">
                                                    {result.quantityOnHand} in stock
                                                </span>
                                            )}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="p-3 text-sm text-center text-muted-foreground">
                            No products or services found.
                            <br />
                            <span className="text-xs">Press Enter to use as custom item.</span>
                        </div>
                    )}
                </Card>,
                document.body
            )}
        </div>
    );
};
