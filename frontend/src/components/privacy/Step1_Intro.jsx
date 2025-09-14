import React from 'react';
import { View, Text, ScrollView } from 'react-native';
import AccordionSection from '../ui/AccordionSection';
import Icon from 'react-native-vector-icons/FontAwesome5';

const Step1_Intro = ({ styles, icon }) => {
  return (
    <View style={{ paddingVertical: 24, paddingHorizontal: 8 }}>
      <Icon name={icon} size={48} style={{ alignSelf: 'center', marginBottom: 16, color: styles.title.color }} />
      <Text style={[styles.bodyText, { marginBottom: 16, textAlign: 'center' }]}>
        Wir freuen uns über Ihr Engagement im TechnikTeam. Der Schutz Ihrer persönlichen Daten ist uns ein wichtiges Anliegen. Nachfolgend informieren wir Sie ausführlich über den Umgang mit Ihren Daten bei der Nutzung unserer Web- und Mobilanwendung "TechnikTeam" (nachfolgend "Anwendung").
      </Text>
      <Text style={[styles.subtitle, { textAlign: 'center' }]}>Stand: 10. September 2025</Text>
      
      <AccordionSection title="1. Verantwortlicher für die Datenverarbeitung">
        <Text style={styles.bodyText}>
          Verantwortlicher im Sinne der DSGVO ist:
          {'\n\n'}
          <Text style={{ fontWeight: 'bold' }}>[Ihr vollständiger Name]</Text>
          {'\n'}[Ihre Straße und Hausnummer]
          {'\n'}[Ihre Postleitzahl und Ihr Ort]
          {'\n'}Deutschland
          {'\n'}E-Mail: [Ihre E-Mail-Adresse für Kontaktanfragen]
        </Text>
      </AccordionSection>
    </View>
  );
};

export default Step1_Intro;