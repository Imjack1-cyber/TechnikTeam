import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import useApi from '../../../hooks/useApi';
import apiClient from '../../../services/apiClient';
import TaskModal from '../../events/TaskModal';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors, spacing, typography } from '../../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const AdminEventTasksTab = ({ event, onUpdate }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    
    // For now, we use the event's tasks. A dedicated category endpoint would be better.
    const categories = Array.from(new Set(event.eventTasks?.map(t => t.categoryId) || []));

    const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
    const [editingTask, setEditingTask] = useState(null);

    const openTaskModal = (task = null) => {
        setEditingTask(task);
        setIsTaskModalOpen(true);
    };

    const handleSuccess = () => {
        setIsTaskModalOpen(false);
        setEditingTask(null);
        onUpdate(); // Reload event details
    };
    
    const renderTaskItem = (task) => (
        <TouchableOpacity key={task.id} style={styles.taskItem} onPress={() => openTaskModal(task)}>
            <View style={{flex: 1}}>
                <Text style={styles.taskName}>{task.name}</Text>
                <Text style={styles.taskStatus}>{task.status}</Text>
            </View>
            <Icon name="edit" size={18} color={colors.textMuted} />
        </TouchableOpacity>
    );

    return (
        <View>
            <TouchableOpacity 
                style={[styles.button, styles.successButton, { alignSelf: 'flex-start', marginBottom: spacing.md }]} 
                onPress={() => openTaskModal()}
            >
                <Icon name="plus" size={16} color={colors.white} />
                <Text style={styles.buttonText}> Neue Aufgabe erstellen</Text>
            </TouchableOpacity>

            <View style={styles.card}>
                <Text style={styles.cardTitle}>Aufgaben nach Kategorie</Text>
                {/* Simplified view without full category management for now */}
                {event.eventTasks && event.eventTasks.length > 0 ? (
                    event.eventTasks.map(task => renderTaskItem(task))
                ) : (
                    <Text>Für dieses Event wurden noch keine Aufgaben erstellt.</Text>
                )}
            </View>

            <TaskModal
                isOpen={isTaskModalOpen}
                onClose={() => setIsTaskModalOpen(false)}
                onSuccess={handleSuccess}
                event={event}
                task={editingTask}
                allUsers={event.assignedAttendees}
            />
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        taskItem: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: spacing.sm,
            borderBottomWidth: 1,
            borderColor: colors.border,
        },
        taskName: {
            fontSize: typography.body,
            fontWeight: 'bold',
        },
        taskStatus: {
            fontSize: typography.small,
            color: colors.textMuted,
        }
    });
};

export default AdminEventTasksTab;