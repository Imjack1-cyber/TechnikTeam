import React from 'react';
import { View, Text, ScrollView } from 'react-native';
import AccordionSection from '../ui/AccordionSection';
import Icon from 'react-native-vector-icons/FontAwesome5';

const Step5_Security = ({ styles, icon }) => {
  return (
    <View style={{ paddingVertical: 24, paddingHorizontal: 8 }}>
      <Icon name={icon} size={48} style={{ alignSelf: 'center', marginBottom: 16, color: styles.title.color }} />
      <Text style={[styles.cardTitle, { textAlign: 'center', borderBottomWidth: 0, paddingBottom: 0 }]}>Datensicherheit & Weitere Hinweise</Text>
      <Text style={[styles.bodyText, { marginBottom: 16, textAlign: 'center' }]}>
        Der Schutz Ihrer Daten ist uns technisch und organisatorisch wichtig.
      </Text>

      <AccordionSection title="5. Datensicherheit">
        <Text style={styles.bodyText}>
          Wir nutzen technische und organisatorische Maßnahmen (u.a. SSL/TLS-Verschlüsselung, Passwort-Hashing), um Ihre Daten zu schützen.
        </Text>
      </AccordionSection>

      <AccordionSection title="6. Keine Verwendung für Werbung oder Tracking">
        <Text style={styles.bodyText}>
          Ihre Daten werden nicht für Werbezwecke verwendet oder an unbeteiligte Dritte verkauft.
        </Text>
      </AccordionSection>
      
      <AccordionSection title="7. Aktualität und Änderung dieser Datenschutzerklärung">
         <Text style={styles.bodyText}>
            Diese Datenschutzerklärung ist aktuell gültig und hat den Stand 10. September 2025.
        </Text>
      </AccordionSection>
    </View>
  );
};

export default Step5_Security;