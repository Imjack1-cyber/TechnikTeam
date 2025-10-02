import React, { useState, useCallback, useMemo, useEffect } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, TextInput, Alert, Platform } from 'react-native';
import { useRoute } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing, typography, borders } from '../styles/theme';
import Stepper from '../components/ui/Stepper';
import { useToast } from '../context/ToastContext';
import { format, parseISO, eachDayOfInterval } from 'date-fns';
import Icon from 'react-native-vector-icons/FontAwesome5';
import Modal from '../components/ui/Modal';
import ScrollableContent from '../components/ui/ScrollableContent';

// --- Reusable, Simple Components ---

const MaybeModal = ({ isOpen, onClose, onConfirm, notes, setNotes }) => {
    const theme = 'light';
    const styles = getCommonStyles(theme);

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Grund für 'Vielleicht'">
            <View>
                <Text style={styles.label}>Bitte gib kurz an, warum du an diesem Tag nur vielleicht kannst (z.B. "kann erst ab 18 Uhr").</Text>
                <TextInput
                    style={[styles.input, styles.textArea]}
                    value={notes}
                    onChangeText={setNotes}
                    autoFocus
                />
                <TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 16}]} onPress={onConfirm}>
                    <Text style={styles.buttonText}>Bestätigen</Text>
                </TouchableOpacity>
            </View>
        </Modal>
    );
};

const DayButton = ({ date, vote, onDayPress, isDisabled, styles, pageStyles }) => {
    let containerStyle = [pageStyles.dayButton];
    let textStyle = [pageStyles.dayButtonText];

    if (isDisabled) {
        containerStyle.push(pageStyles.dayButtonDisabled);
        textStyle.push(pageStyles.dayButtonTextDisabled);
    } else {
        if (vote?.status === 'AVAILABLE') containerStyle.push(pageStyles.available);
        if (vote?.status === 'MAYBE') containerStyle.push(pageStyles.maybe);
        if (vote?.status === 'UNAVAILABLE') containerStyle.push(pageStyles.unavailable);
    }
    
    return (
        <TouchableOpacity style={containerStyle} onPress={() => !isDisabled && onDayPress(date)} disabled={isDisabled}>
            <Text style={textStyle}>{format(date, 'd')}</Text>
            <Text style={[textStyle, {fontSize: 10}]}>{format(date, 'eee')}</Text>
        </TouchableOpacity>
    );
};

// --- Main Page Component ---

const SchedulingPollPage = () => {
    const route = useRoute();
    const { uuid } = route.params;
    const { addToast } = useToast();
    const { user, isAuthenticated, logout } = useAuthStore();
    
    const theme = 'light'; 
    const styles = getCommonStyles(theme);
    const pageStylesInstance = pageStyles(theme);
    const colors = getThemeColors(theme);

    const apiCall = useCallback(() => apiClient.get(`/public/polls/${uuid}`), [uuid]);
    const { data: pollData, loading, error: apiError } = useApi(apiCall);

    const [step, setStep] = useState(0);
    const [identity, setIdentity] = useState({ guestName: '', verificationCode: '' });
    const [response, setResponse] = useState({ notes: '', dayVotes: {} });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [isMaybeModalOpen, setIsMaybeModalOpen] = useState(false);
    const [currentMaybeDate, setCurrentMaybeDate] = useState(null);
    const [maybeNotes, setMaybeNotes] = useState('');

    useEffect(() => {
        if (pollData && isAuthenticated && pollData.responders?.includes(user.username)) {
            setStep(3); // Skip to Thank You page if user has voted
        }
    }, [pollData, isAuthenticated, user]);

    const handleLogoutAndParticipate = () => {
        logout();
        setStep(0);
        setIdentity({ guestName: '', verificationCode: '' });
    };

    const handleDayPress = (date) => {
        const dateString = format(date, 'yyyy-MM-dd');
        const currentVote = response.dayVotes[dateString];

        if (currentVote?.status === 'AVAILABLE') {
            setCurrentMaybeDate(dateString);
            setMaybeNotes(currentVote.notes || '');
            setIsMaybeModalOpen(true);
        } else if (currentVote?.status === 'MAYBE') {
            setResponse(prev => ({ ...prev, dayVotes: { ...prev.dayVotes, [dateString]: { status: 'UNAVAILABLE', notes: null } } }));
        } else if (currentVote?.status === 'UNAVAILABLE') {
            setResponse(prev => {
                const newVotes = { ...prev.dayVotes };
                delete newVotes[dateString];
                return { ...prev, dayVotes: newVotes };
            });
        } else {
            setResponse(prev => ({ ...prev, dayVotes: { ...prev.dayVotes, [dateString]: { status: 'AVAILABLE', notes: null } } }));
        }
    };

    const handleConfirmMaybe = () => {
        setResponse(prev => ({ ...prev, dayVotes: { ...prev.dayVotes, [currentMaybeDate]: { status: 'MAYBE', notes: maybeNotes } } }));
        setIsMaybeModalOpen(false);
        setCurrentMaybeDate(null);
    };

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setError('');
        try {
            const payload = {
                guestName: isAuthenticated ? null : identity.guestName,
                verificationCode: identity.verificationCode,
                notes: response.notes,
                dayVotes: Object.entries(response.dayVotes).map(([date, vote]) => ({ date, ...vote })),
            };
            const result = await apiClient.post(`/public/polls/${uuid}/respond`, payload);
            if (result.success) {
                setStep(3);
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            setError(err.message || 'Ein Fehler ist aufgetreten.');
        } finally {
            setIsSubmitting(false);
        }
    };
    
    if (loading) return <View style={styles.centered}><ActivityIndicator /></View>;
    if (apiError || !pollData) return (
        <View style={styles.centered}>
            <Text style={styles.title}>Umfrage nicht gefunden</Text>
            <Text style={styles.subtitle}>{apiError || 'Dieser Link ist ungültig oder die Umfrage wurde gelöscht.'}</Text>
        </View>
    );
    
    const { poll, options, responders } = pollData;
    const adminAvailableDays = new Set(options?.availableDays || []);

    const renderStepContent = () => {
        switch (step) {
            case 0: // Identity
                return (
                    <View>
                        <Text style={styles.title}>Willkommen zur Umfrage</Text>
                        <Text style={styles.subtitle}>{poll.title}</Text>
                        {isAuthenticated ? (
                            <View>
                                <Text style={{textAlign: 'center', marginBottom: spacing.md}}>Du bist angemeldet als {user.username}.</Text>
                                <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={() => setStep(1)}>
                                    <Text style={styles.buttonText}>Weiter als {user.username}</Text>
                                </TouchableOpacity>
                                <TouchableOpacity style={[styles.button, styles.secondaryButton, {marginTop: 8}]} onPress={handleLogoutAndParticipate}>
                                    <Text style={styles.buttonText}>Abmelden & als Gast teilnehmen</Text>
                                </TouchableOpacity>
                            </View>
                        ) : (
                             <View>
                                <Text style={styles.label}>Dein Name (als Gast)</Text>
                                <TextInput style={styles.input} value={identity.guestName} onChangeText={val => setIdentity({...identity, guestName: val})} placeholder="Max Mustermann"/>
                                {options.requireVerificationCode && (
                                    <>
                                        <Text style={styles.label}>Verifizierungscode</Text>
                                        <TextInput style={styles.input} value={identity.verificationCode} onChangeText={val => setIdentity({...identity, verificationCode: val})} placeholder="Code"/>
                                    </>
                                )}
                                <TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: spacing.md}]} onPress={() => {
                                    if (responders?.includes(identity.guestName)) {
                                        setStep(3);
                                    } else {
                                        setStep(1);
                                    }
                                }} disabled={!identity.guestName || (options.requireVerificationCode && !identity.verificationCode)}>
                                    <Text style={styles.buttonText}>Weiter als Gast</Text>
                                </TouchableOpacity>
                            </View>
                        )}
                    </View>
                );
            case 1: // Date Selection
                const pollDays = eachDayOfInterval({ start: parseISO(poll.startTime), end: parseISO(poll.endTime) });
                return (
                     <View>
                        <Text style={styles.title}>{poll.title}</Text>
                        <Text style={styles.subtitle}>Klicke auf ein Datum: 1x=Kann, 2x=Vielleicht, 3x=Kann nicht, 4x=Zurücksetzen.</Text>
                        <View style={pageStylesInstance.calendarGrid}>
                            {pollDays.map(day => (
                                <DayButton
                                    key={day.toISOString()}
                                    date={day}
                                    vote={response.dayVotes[format(day, 'yyyy-MM-dd')]}
                                    isDisabled={!adminAvailableDays.has(format(day, 'yyyy-MM-dd'))}
                                    onDayPress={handleDayPress}
                                    styles={styles}
                                    pageStyles={pageStylesInstance}
                                />
                            ))}
                        </View>
                    </View>
                );
            case 2: // Notes
                 return (
                    <View>
                        <Text style={styles.title}>Fast fertig!</Text>
                        <Text style={styles.label}>Anmerkungen (optional)</Text>
                        <TextInput style={[styles.input, styles.textArea]} value={response.notes} onChangeText={val => setResponse({...response, notes: val})} placeholder="Zusätzliche Informationen..."/>
                        {error && <Text style={styles.errorText}>{error}</Text>}
                    </View>
                 );
            case 3: // Thank You
                 return (
                    <View style={{alignItems: 'center'}}>
                        <Icon name="check-circle" solid size={60} color={colors.success} style={{marginBottom: spacing.md}}/>
                        <Text style={styles.title}>Vielen Dank!</Text>
                        <Text style={styles.subtitle}>Deine Antwort wurde erfolgreich übermittelt.</Text>
                    </View>
                 );
            default: return null;
        }
    };

    return (
        <ScrollableContent contentContainerStyle={styles.centered}>
            <View style={styles.card}>
                <Stepper steps={['Identität', 'Auswahl', 'Abschluss', 'Fertig']} currentStep={step} />
                <View style={{padding: spacing.md, minHeight: 400, justifyContent: 'center'}}>
                    {renderStepContent()}
                </View>
                 {step < 3 && (
                    <View style={{flexDirection: 'row', justifyContent: 'space-between', padding: spacing.md, borderTopWidth: 1, borderColor: colors.border}}>
                        <TouchableOpacity style={[styles.button, styles.secondaryButton, step === 0 && styles.disabledButton]} onPress={() => setStep(s => s - 1)} disabled={step === 0}>
                            <Text style={styles.buttonText}>Zurück</Text>
                        </TouchableOpacity>
                        {step < 2 ? (
                            <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={() => setStep(s => s + 1)}>
                                <Text style={styles.buttonText}>Weiter</Text>
                            </TouchableOpacity>
                        ) : (
                            <TouchableOpacity style={[styles.button, styles.successButton]} onPress={handleSubmit} disabled={isSubmitting}>
                                {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Antwort senden</Text>}
                            </TouchableOpacity>
                        )}
                    </View>
                )}
            </View>
            <MaybeModal 
                isOpen={isMaybeModalOpen}
                onClose={() => setIsMaybeModalOpen(false)}
                onConfirm={handleConfirmMaybe}
                notes={maybeNotes}
                setNotes={setMaybeNotes}
            />
        </ScrollableContent>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        calendarGrid: {
            flexDirection: 'row',
            flexWrap: 'wrap',
            justifyContent: 'center',
        },
        dayButton: {
            width: 60,
            height: 60,
            borderRadius: 8,
            margin: 4,
            justifyContent: 'center',
            alignItems: 'center',
            borderWidth: 1,
            borderColor: colors.border,
            backgroundColor: colors.background,
        },
        dayButtonText: {
            fontSize: 18,
            fontWeight: 'bold',
            color: colors.text,
        },
        dayButtonDisabled: {
            backgroundColor: colors.border,
            opacity: 0.5,
        },
        dayButtonTextDisabled: {
            color: colors.textMuted,
        },
        available: {
            backgroundColor: colors.success,
            borderColor: colors.success,
        },
        maybe: {
            backgroundColor: colors.warning,
            borderColor: colors.warning,
        },
        unavailable: {
            backgroundColor: colors.danger,
            borderColor: colors.danger,
        }
    });
};

export default SchedulingPollPage;