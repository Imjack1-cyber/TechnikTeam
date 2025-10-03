import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../../store/authStore';
import { getThemeColors, spacing } from '../../../styles/theme';

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

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        treeItem: { flexDirection: 'row', alignItems: 'center', paddingVertical: spacing.sm },
        activeTreeItem: { backgroundColor: colors.primaryLight },
        treeIcon: { marginRight: spacing.sm, color: colors.textMuted, width: 20 },
    });
};

export default WikiTreeNode;