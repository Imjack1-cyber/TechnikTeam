import React, { useState, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, SectionList, TouchableOpacity, ActivityIndicator, TextInput, Image } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import apiClient from '../services/apiClient';
import useApi from '../hooks/useApi';
import Lightbox from '../components/ui/Lightbox';
import { useToast } from '../context/ToastContext';
import CartModal from '../components/storage/CartModal';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const AvailabilityBar = ({ available, max }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    if (max === 0) return <View style={{ height: 8, backgroundColor: colors.success, borderRadius: 4 }} />;
    const percentage = Math.max(0, (available / max) * 100);
    let color = colors.success;
    if (percentage <= 25) color = colors.danger;
    else if (percentage <= 50) color = colors.warning;

    return (
        <View style={{ height: 8, backgroundColor: colors.background, borderRadius: 4, overflow: 'hidden' }}>
            <View style={{ width: `${percentage}%`, height: '100%', backgroundColor: color }} />
        </View>
    );
};

const StoragePage = () => {
    const navigation = useNavigation();
    const apiCall = useCallback(() => apiClient.get('/public/storage'), []);
    const { data, loading, error, reload } = useApi(apiCall);
    const [cart, setCart] = useState([]);
    const [isLightboxOpen, setIsLightboxOpen] = useState(false);
    const [lightboxSrc, setLightboxSrc] = useState('');
    const [isCartOpen, setIsCartOpen] = useState(false);
    const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = pageStyles(theme);
    const colors = getThemeColors(theme);
    const [searchTerm, setSearchTerm] = useState('');

    const sections = useMemo(() => {
        const storageData = data?.storageData || {};
        const filteredAndGrouped = Object.entries(storageData).map(([location, items]) => {
            const filteredItems = items.filter(item =>
                item.name.toLowerCase().includes(searchTerm.toLowerCase())
            );
            return { title: location, data: filteredItems };
        }).filter(section => section.data.length > 0);

        // Custom sort order: "Erdgeschoss" first, then "Oben", then alphabetically.
        return filteredAndGrouped.sort((a, b) => {
            const aTitle = a.title.toLowerCase();
            const bTitle = b.title.toLowerCase();
            if (aTitle.includes('erdgeschoss')) return -1;
            if (bTitle.includes('erdgeschoss')) return 1;
            if (aTitle.includes('oben')) return -1;
            if (bTitle.includes('oben')) return 1;
            return a.title.localeCompare(b.title);
        });
    }, [data, searchTerm]);

    const handleAddToCart = (item, type) => {
        setCart(prev => [...prev, { ...item, cartQuantity: 1, type }]);
        addToast(`${item.name} zum Warenkorb hinzugefügt.`, 'success');
    };

    const handleUpdateCartQuantity = (itemId, type, newQuantity) => {
        setCart(prev => prev.map(item =>
            item.id === itemId && item.type === type ? { ...item, cartQuantity: newQuantity } : item
        ));
    };

    const handleRemoveFromCart = (itemId, type) => {
        setCart(prev => prev.filter(item => !(item.id === itemId && item.type === type)));
    };

    const handleSwitchCartItemType = (itemId, oldType) => {
        setCart(prev => prev.map(item =>
            item.id === itemId && item.type === oldType ? { ...item, type: oldType === 'checkout' ? 'checkin' : 'checkout' } : item
        ));
    };


    const getImagePath = (path) => `${apiClient.getRootUrl()}/api/v1/public/files/images/${path.split('/').pop()}`;

    const renderItem = ({ item }) => (
        <View style={commonStyles.card}>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: spacing.sm }}>
                {item.imagePath ? (
                    <TouchableOpacity onPress={() => { setLightboxSrc(getImagePath(item.imagePath)); setIsLightboxOpen(true); }}>
                        <Image source={{ uri: getImagePath(item.imagePath) }} style={styles.itemImage} />
                    </TouchableOpacity>
                ) : (
                    <View style={[styles.itemImage, styles.imagePlaceholder]}>
                        <Icon name="box-open" size={24} color={colors.textMuted} />
                    </View>
                )}
                <TouchableOpacity style={{ flex: 1 }} onPress={() => navigation.navigate('StorageItemDetails', { itemId: item.id })}>
                    <Text style={commonStyles.cardTitle}>{item.name}</Text>
                </TouchableOpacity>
            </View>
            <AvailabilityBar available={item.availableQuantity} max={item.maxQuantity} />
            <Text style={styles.quantityText}>{item.availableQuantity} / {item.maxQuantity} Verfügbar</Text>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[commonStyles.button, commonStyles.dangerOutlineButton]} onPress={() => handleAddToCart(item, 'checkout')} disabled={item.availableQuantity <= 0}>
                    <Icon name="minus" size={14} color={colors.danger} />
                    <Text style={commonStyles.dangerOutlineButtonText}> Entnehmen</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[commonStyles.button, commonStyles.successButton]} onPress={() => handleAddToCart(item, 'checkin')}>
                    <Icon name="plus" size={14} color={colors.white} />
                    <Text style={commonStyles.buttonText}> Einräumen</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

    const renderSectionHeader = ({ section: { title } }) => (
        <Text style={styles.sectionHeader}>{title}</Text>
    );

    if (loading) return <View style={commonStyles.centered}><ActivityIndicator size="large" /></View>;
    if (error) return <View style={commonStyles.centered}><Text style={commonStyles.errorText}>{error}</Text></View>;

    return (
        <>
            <SectionList
                sections={sections}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                renderSectionHeader={renderSectionHeader}
                stickySectionHeadersEnabled={true}
                contentContainerStyle={{ padding: spacing.md, paddingBottom: 80 }}
                ListHeaderComponent={
                    <>
                        <Text style={commonStyles.title}>Lagerübersicht</Text>
                        <Text style={commonStyles.subtitle}>Übersicht aller erfassten Artikel im Lager.</Text>
                        <TextInput
                            style={[commonStyles.input, {marginBottom: spacing.md}]}
                            placeholder="Artikel suchen..."
                            value={searchTerm}
                            onChangeText={setSearchTerm}
                        />
                    </>
                }
                ListEmptyComponent={
                    <View style={commonStyles.card}>
                        <Text>Keine Artikel für Ihre Suche gefunden.</Text>
                    </View>
                }
            />
            {cart.length > 0 && (
                <TouchableOpacity style={styles.fab} onPress={() => setIsCartOpen(true)}>
                    <Icon name="shopping-cart" size={24} color="#fff" />
                    <View style={styles.fabBadge}>
                        <Text style={styles.fabBadgeText}>{cart.length}</Text>
                    </View>
                </TouchableOpacity>
            )}
            {isLightboxOpen && <Lightbox src={lightboxSrc} onClose={() => setIsLightboxOpen(false)} />}
            <CartModal
                isOpen={isCartOpen}
                onClose={() => setIsCartOpen(false)}
                cart={cart}
                onUpdateQuantity={handleUpdateCartQuantity}
                onRemove={handleRemoveFromCart}
                onSwitchType={handleSwitchCartItemType}
                activeEvents={data?.activeEvents || []}
                onSuccess={() => {
                    setIsCartOpen(false);
                    setCart([]);
                    reload();
                }}
            />
        </>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        itemImage: { width: 50, height: 50, borderRadius: 8 },
        imagePlaceholder: {
            backgroundColor: colors.background,
            justifyContent: 'center',
            alignItems: 'center',
            borderWidth: 1,
            borderColor: colors.border,
        },
        quantityText: { fontSize: typography.small, color: colors.textMuted, marginTop: spacing.xs },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: spacing.sm, marginTop: spacing.md },
        fab: {
            position: 'absolute',
            margin: 16,
            right: 0,
            bottom: 0,
            backgroundColor: colors.primary,
            width: 60,
            height: 60,
            borderRadius: 30,
            justifyContent: 'center',
            alignItems: 'center',
            elevation: 8,
        },
        fabBadge: {
            position: 'absolute',
            right: -2,
            top: -2,
            backgroundColor: colors.danger,
            borderRadius: 10,
            width: 20,
            height: 20,
            justifyContent: 'center',
            alignItems: 'center',
        },
        fabBadgeText: {
            color: 'white',
            fontSize: 10,
            fontWeight: 'bold',
        },
        sectionHeader: {
            fontSize: typography.h3,
            fontWeight: 'bold',
            color: colors.heading,
            padding: spacing.sm,
            backgroundColor: colors.background,
            marginTop: spacing.md,
            marginBottom: spacing.sm,
        }
    });
};


export default StoragePage;