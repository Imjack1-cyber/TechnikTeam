import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, TextInput, useWindowDimensions } from 'react-native';
import { createStackNavigator } from '@react-navigation/stack';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import WikiPageModal from '../../components/admin/wiki/WikiPageModal';
import ScrollableContent from '../../components/ui/ScrollableContent';
import WikiEditorPage from './WikiEditorPage';

const WikiStack = createStackNavigator();

const WikiTreeNode = ({ name, node, onSelect, selectedId, level = 0 }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);
    const [isOpen, setIsOpen] = useState(true);

    if (node.id) { // It's a file
        return (
            <TouchableOpacity onPress={() => onSelect(node)} style={[styles.treeItem, { paddingLeft: level * spacing.md }, selectedId === node.id && styles.activeTreeItem]}>
                <Icon name="file-alt" style={styles.treeIcon} />
                <Text style={{color: colors.text}}>{name}</Text>
            </TouchableOpacity>
        );
    }

    // It's a directory
    return (
        <View>
            <TouchableOpacity onPress={() => setIsOpen(!isOpen)} style={[styles.treeItem, { paddingLeft: level * spacing.md }]}>
                <Icon name={isOpen ? "folder-open" : "folder"} solid style={styles.treeIcon} />
                <Text style={{fontWeight: 'bold', color: colors.text}}>{name}</Text>
            </TouchableOpacity>
            {isOpen && (
                <View>
                    {Object.entries(node).map(([childName, childNode]) => (
                        <WikiTreeNode key={childName} name={childName} node={childNode} onSelect={onSelect} selectedId={selectedId} level={level + 1} />
                    ))}
                </View>
            )}
        </View>
    );
};

const WikiListPage = ({ navigation }) => {
    const treeApiCall = useCallback(() => apiClient.get('/wiki'), []);
	const { data: wikiTree, loading, error, reload } = useApi(treeApiCall, { subscribeTo: 'WIKI' });
    const [isModalOpen, setIsModalOpen] = useState(false);
    const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

    const handleSelectEntry = (entry) => {
        navigation.navigate('WikiEditor', { entryId: entry.id, entryPath: entry.filePath });
    };

    if (loading) return <View style={styles.centered}><ActivityIndicator size="large"/></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;

    return (
        <View style={styles.container}>
            <View style={styles.sidebarHeader}>
                <Text style={styles.title}>Wiki-Verzeichnis</Text>
                <TouchableOpacity onPress={() => setIsModalOpen(true)}><Icon name="plus" size={20} /></TouchableOpacity>
            </View>
            <ScrollableContent contentContainerStyle={styles.contentContainer}>
                {wikiTree && Object.entries(wikiTree).map(([name, node]) => (
                    <WikiTreeNode key={name} name={name} node={node} onSelect={handleSelectEntry} selectedId={null} />
                ))}
            </ScrollableContent>
            <WikiPageModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={() => {setIsModalOpen(false); reload();}} parentPath={""}/>
        </View>
    );
};

const AdminWikiPage = () => {
    return (
        <WikiStack.Navigator screenOptions={{ headerShown: false }}>
            <WikiStack.Screen name="WikiList" component={WikiListPage} />
            <WikiStack.Screen name="WikiEditor" component={WikiEditorPage} />
        </WikiStack.Navigator>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        sidebarHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: spacing.md },
        treeItem: { flexDirection: 'row', alignItems: 'center', paddingVertical: spacing.sm },
        activeTreeItem: { backgroundColor: colors.primaryLight },
        treeIcon: { marginRight: spacing.sm, color: colors.textMuted, width: 20 },
    });
};

export default AdminWikiPage;