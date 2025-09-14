import React, { useRef, useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ActivityIndicator,
  StyleSheet,
  SafeAreaView,
  ScrollView,
} from 'react-native';
import { useAuthStore } from '../store/authStore';
import { useToast } from '../context/ToastContext';
import apiClient from '../services/apiClient';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing } from '../styles/theme';
import Stepper from '../components/ui/Stepper';
import Icon from 'react-native-vector-icons/FontAwesome5';

// Import step components
import Step1_Intro from '../components/privacy/Step1_Intro';
import Step2_DataTypes from '../components/privacy/Step2_DataTypes';
import Step3_Recipients from '../components/privacy/Step3_Recipients';
import Step4_YourRights from '../components/privacy/Step4_YourRights';
import Step5_Security from '../components/privacy/Step5_Security';
import Step6_Agreement from '../components/privacy/Step6_Agreement';


const PRIVACY_POLICY_VERSION = "2025-09-10";

const steps = [
    { title: 'Einleitung', icon: 'file-contract', component: Step1_Intro },
    { title: 'Ihre Daten', icon: 'server', component: Step2_DataTypes },
    { title: 'Empfänger', icon: 'globe-americas', component: Step3_Recipients },
    { title: 'Ihre Rechte', icon: 'user-shield', component: Step4_YourRights },
    { title: 'Sicherheit', icon: 'lock', component: Step5_Security },
    { title: 'Zustimmung', icon: 'check-circle', component: Step6_Agreement },
];

const PrivacyPolicyPage = () => {
  const { fetchUserSession, theme } = useAuthStore();
  const { addToast } = useToast();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isChecked, setIsChecked] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);

  const styles = getCommonStyles(theme);
  const colors = getThemeColors(theme);

  const handleSubmit = async () => {
    if (!isChecked) return;
    setIsSubmitting(true);
    try {
      const result = await apiClient.post('/public/profile/accept-privacy-policy', {
        version: PRIVACY_POLICY_VERSION,
      });
      if (result.success) {
        addToast('Vielen Dank!', 'success');
        await fetchUserSession();
      } else {
        throw new Error(result.message);
      }
    } catch (error) {
      addToast(`Ein Fehler ist aufgetreten: ${error.message}`, 'error');
      setIsSubmitting(false);
    }
  };

  const CurrentStepComponent = steps[currentStep].component;

  return (
    <SafeAreaView style={styles.container}>
      <View style={[styles.card, pageStyles.cardContainer]}>
        <ScrollView contentContainerStyle={pageStyles.scrollContent}>
          {/* Header */}
          <View style={pageStyles.header}>
            <Text style={styles.title}>Datenschutzerklärung</Text>
            <Stepper steps={steps.map(s => s.title)} currentStep={currentStep} />
          </View>

          {/* Dynamic Content */}
          <View style={pageStyles.stepContentWrapper}>
            <CurrentStepComponent styles={styles} isChecked={isChecked} onToggleCheck={setIsChecked} icon={steps[currentStep].icon} />
          </View>

          {/* Footer */}
          <View style={pageStyles.footer}>
              <View style={pageStyles.navigationButtons}>
                  <TouchableOpacity
                      style={[styles.button, styles.secondaryButton, currentStep === 0 && styles.disabledButton]}
                      disabled={currentStep === 0}
                      onPress={() => setCurrentStep(s => s - 1)}
                  >
                      <Icon name="arrow-left" size={16} color={colors.white} />
                      <Text style={styles.buttonText}>Zurück</Text>
                  </TouchableOpacity>

                  {currentStep < steps.length - 1 ? (
                      <TouchableOpacity
                          style={[styles.button, styles.primaryButton]}
                          onPress={() => setCurrentStep(s => s + 1)}
                      >
                          <Text style={styles.buttonText}>Weiter</Text>
                          <Icon name="arrow-right" size={16} color={colors.white} />
                      </TouchableOpacity>
                  ) : (
                      <TouchableOpacity
                          style={[
                              styles.button,
                              styles.successButton,
                              (!isChecked || isSubmitting) && styles.disabledButton,
                          ]}
                          disabled={!isChecked || isSubmitting}
                          onPress={handleSubmit}
                      >
                          {isSubmitting ? (
                              <ActivityIndicator color="#fff" />
                          ) : (
                              <>
                                  <Text style={styles.buttonText}>Zustimmen & Weiter</Text>
                                  <Icon name="check" size={16} color={colors.white} />
                              </>
                          )}
                      </TouchableOpacity>
                  )}
              </View>
          </View>
        </ScrollView>
      </View>
    </SafeAreaView>
  );
};

const pageStyles = StyleSheet.create({
  cardContainer: {
    flex: 1,
    maxWidth: 800,
    width: '100%',
    padding: 0, // Padding is now handled by the ScrollView content
  },
  scrollContent: {
    flexGrow: 1, // Makes content expand to fill space, crucial for short steps
    padding: spacing.lg, // Apply padding inside the scrollable area
    flexDirection: 'column', // Ensure vertical layout
  },
  header: {
    paddingBottom: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: '#ddd',
  },
  stepContentWrapper: {
    flex: 1, // This makes the content area expand, pushing the footer down
  },
  footer: {
    paddingTop: spacing.md,
    borderTopWidth: 1,
    borderTopColor: '#ddd',
  },
  navigationButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
});

export default PrivacyPolicyPage;