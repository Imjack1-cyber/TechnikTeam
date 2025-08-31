import React, { useState, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, TextInput, Image } from 'react-native';
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
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const handleAddToCart = (item, type) => {
		setCart(prev => [...prev, { ...item, cartQuantity: 1, type }]);
		addToast(`${item.name} zum Warenkorb hinzugefügt.`, 'success');
	};

	const getImagePath = (path) => `http://10.0.2.2:8081/TechnikTeam/api/v1/public/files/images/${path.split('/').pop()}`;

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <View style={{flexDirection: 'row', alignItems: 'center', gap: spacing.sm}}>
                {item.imagePath && (
                    <TouchableOpacity onPress={() => { setLightboxSrc(getImagePath(item.imagePath)); setIsLightboxOpen(true); }}>
                        <Image source={{ uri: getImagePath(item.imagePath) }} style={styles.itemImage} />
                    </TouchableOpacity>
                )}
                <TouchableOpacity style={{flex: 1}} onPress={() => navigation.navigate('StorageItemDetails', { itemId: item.id })}>
                    <Text style={styles.cardTitle}>{item.name}</Text>
                </TouchableOpacity>
            </View>
            <AvailabilityBar available={item.availableQuantity} max={item.maxQuantity} />
            <Text style={styles.quantityText}>{item.availableQuantity} / {item.maxQuantity} Verfügbar</Text>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => handleAddToCart(item, 'checkout')} disabled={item.availableQuantity <= 0}>
                    <Icon name="minus" size={14} color={colors.danger} />
                    <Text style={styles.dangerOutlineButtonText}> Entnehmen</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => handleAddToCart(item, 'checkin')}>
                    <Icon name="plus" size={14} color={colors.white} />
                    <Text style={styles.buttonText}> Einräumen</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
    
    const allItems = Object.values(data?.storageData || {}).flat();

	return (
		<>
			<FlatList
                data={allItems}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{padding: spacing.md}}
                ListHeaderComponent={
                    <>
                        <Text style={styles.title}>Lagerübersicht</Text>
                        <Text style={styles.subtitle}>Übersicht aller erfassten Artikel im Lager.</Text>
                    </>
                }
            />
			{isLightboxOpen && <Lightbox src={lightboxSrc} onClose={() => setIsLightboxOpen(false)} />}
            {/* CartModal would need to be implemented */}
		</>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        itemImage: { width: 50, height: 50, borderRadius: 8 },
        quantityText: { fontSize: typography.small, color: colors.textMuted, marginTop: spacing.xs },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: spacing.sm, marginTop: spacing.md },
    });
};


export default StoragePage;