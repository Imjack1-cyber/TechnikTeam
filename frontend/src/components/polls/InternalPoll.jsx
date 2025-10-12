import React, { useState, useMemo } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, ScrollView, TextInput } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import { RadioButton } from 'react-native-paper';
import ProgressBar from '../ui/ProgressBar';
import { useToast } from '../../context/ToastContext';
import apiClient from '../../services/apiClient';

const WordCloud = ({ data }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = wordCloudStyles({ colors });

    const { maxCount, minCount, words } = useMemo(() => {
        if (!data || data.length === 0) {
            return { maxCount: 1, minCount: 1, words: [] };
        }
        const counts = data.map(item => item.value);
        const max = Math.max(...counts);
        const min = Math.min(...counts);
        const shuffled = [...data].sort(() => 0.5 - Math.random());
        return { maxCount: max, minCount: min, words: shuffled };
    }, [data]);

    const getFontSize = (count) => {
        if (maxCount === minCount) return 18;
        const minSize = 14;
        const maxSize = 48;
        const sizeRange = maxSize - minSize;
        const countRange = maxCount - minCount;
        const scaled = ((count - minCount) / countRange) * sizeRange + minSize;
        return Math.round(scaled);
    };

    if (words.length === 0) {
        return <Text style={{color: colors.textMuted, fontStyle: 'italic'}}>Noch keine Antworten eingegangen.</Text>;
    }

    return (
        <View style={styles.container}>
            {words.map((word, index) => (
                <Text
                    key={index}
                    style={[
                        styles.word,
                        { fontSize: getFontSize(word.value) }
                    ]}
                >
                    {word.text}
                </Text>
            ))}
        </View>
    );
};

const wordCloudStyles = ({ colors }) => StyleSheet.create({
    container: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        justifyContent: 'center',
        alignItems: 'center',
        padding: spacing.md,
        backgroundColor: colors.background,
        borderRadius: 8,
    },
    word: {
        color: colors.primary,
        paddingHorizontal: spacing.sm,
        paddingVertical: spacing.xs,
        fontWeight: '600',
    },
});


const InternalPoll = ({ pollData, reload }) => {
    const { user, isAuthenticated } = useAuthStore();
    const { addToast } = useToast();
    const { poll } = pollData;
    
    const [selectedOption, setSelectedOption] = useState(null);
    const [word, setWord] = useState('');
    const [guestName, setGuestName] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const pollOptions = poll.optionsMap || {};
    const pollType = poll.type;
    
    const handleSubmit = async () => {
        setIsSubmitting(true);
        try {
            let payload = {};
            let endpoint;
            if (isAuthenticated) {
                endpoint = `/public/polls/${poll.id}/vote`;
            } else {
                endpoint = `/public/polls/by-uuid/${poll.uuid}/vote`;
                payload.guestName = guestName;
            }

            if (pollType === 'WORD_CLOUD') {
                payload.word = word;
            } else {
                payload.pollOptionId = selectedOption;
            }

            const result = await apiClient.post(endpoint, payload);
            if (result.success) {
                addToast('Stimme erfolgreich abgegeben.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
        }
    };
    
    const showResults = poll.isClosed || poll.hasVoted;

    const renderMultipleChoice = () => (
        showResults ? (
            <View>
                <Text style={styles.cardTitle}>Ergebnisse</Text>
                {poll.pollOptions?.map(option => (
                    <View key={option.id} style={styles.resultRow}>
                        <View style={{flexDirection: 'row', justifyContent: 'space-between', marginBottom: spacing.xs}}>
                            <Text style={styles.optionText}>{option.optionText}</Text>
                            <Text style={styles.voteCount}>{option.voteCount} Stimme(n) ({option.votePercentage.toFixed(1)}%)</Text>
                        </View>
                        <ProgressBar progress={option.votePercentage / 100} />
                    </View>
                ))}
            </View>
        ) : (
            <View>
                <Text style={styles.cardTitle}>Stimme abgeben</Text>
                {!isAuthenticated && pollOptions.allowGuests && (
                     <TextInput style={styles.input} value={guestName} onChangeText={setGuestName} placeholder="Dein Name (als Gast)"/>
                )}
                <RadioButton.Group onValueChange={newValue => setSelectedOption(newValue)} value={selectedOption}>
                    {poll.pollOptions?.map(option => (
                        <View key={option.id} style={{ flexDirection: 'row', alignItems: 'center' }}>
                            <RadioButton value={option.id} />
                            <Text style={{color: colors.text}}>{option.optionText}</Text>
                        </View>
                    ))}
                </RadioButton.Group>
                <TouchableOpacity style={[styles.button, styles.successButton, {marginTop: spacing.md}]} onPress={handleSubmit} disabled={isSubmitting || !selectedOption || (!isAuthenticated && pollOptions.allowGuests && !guestName.trim())}>
                    <Text style={styles.buttonText}>Abstimmen</Text>
                </TouchableOpacity>
            </View>
        )
    );

    const renderWordCloud = () => (
         showResults ? (
            <View>
                <Text style={styles.cardTitle}>Ergebnisse</Text>
                <WordCloud data={poll.wordCloudResults} />
            </View>
        ) : (
             <View>
                <Text style={styles.cardTitle}>Dein Wort einreichen</Text>
                 {!isAuthenticated && pollOptions.allowGuests && (
                     <TextInput style={styles.input} value={guestName} onChangeText={setGuestName} placeholder="Dein Name (als Gast)"/>
                )}
                <TextInput style={[styles.input, {marginTop: 8}]} value={word} onChangeText={setWord} placeholder="Gib ein Wort ein..."/>
                <TouchableOpacity style={[styles.button, styles.successButton, {marginTop: spacing.md}]} onPress={handleSubmit} disabled={isSubmitting || !word.trim() || (!isAuthenticated && pollOptions.allowGuests && !guestName.trim())}>
                    <Text style={styles.buttonText}>Einreichen</Text>
                </TouchableOpacity>
            </View>
        )
    );

    return (
        <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
            <View style={styles.header}>
                <Text style={styles.title}>{poll.question}</Text>
                <Text style={styles.subtitle}>Erstellt von {poll.createdByUsername}</Text>
            </View>
            <View style={styles.card}>
                {pollType === 'WORD_CLOUD' ? renderWordCloud() : renderMultipleChoice()}
            </View>
        </ScrollView>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: { flex: 1, backgroundColor: colors.background },
        contentContainer: { padding: spacing.md },
        header: { marginBottom: spacing.md },
        resultRow: { marginBottom: spacing.md },
        optionText: { fontSize: typography.body, fontWeight: '500', color: colors.text },
        voteCount: { color: colors.textMuted },
    });
};

export default InternalPoll;