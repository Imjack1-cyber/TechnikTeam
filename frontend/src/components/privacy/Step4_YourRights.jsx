import React from 'react';
import { View, Text, ScrollView } from 'react-native';
import AccordionSection from '../ui/AccordionSection';
import Icon from 'react-native-vector-icons/FontAwesome5';

const Step4_YourRights = ({ styles, icon }) => {
  return (
    <View style={{ paddingVertical: 24, paddingHorizontal: 8 }}>
      <Icon name={icon} size={48} style={{ alignSelf: 'center', marginBottom: 16, color: styles.title.color }} />
      <Text style={[styles.cardTitle, { textAlign: 'center', borderBottomWidth: 0, paddingBottom: 0 }]}>Ihre Rechte als Betroffener</Text>
      <Text style={[styles.bodyText, { marginBottom: 16, textAlign: 'center' }]}>
        Die DSGVO gibt Ihnen umfassende Rechte über Ihre personenbezogenen Daten.
      </Text>

      <AccordionSection title="Ihre grundlegenden Rechte">
        <Text style={styles.bodyText}>
          Sie haben jederzeit das Recht auf:
          {'\n'}• <Text style={{ fontWeight: 'bold' }}>Auskunft</Text> über die von uns verarbeiteten Daten (Art. 15 DSGVO).
          {'\n'}• <Text style={{ fontWeight: 'bold' }}>Berichtigung</Text> unrichtiger Daten (Art. 16 DSGVO).
          {'\n'}• <Text style={{ fontWeight: 'bold' }}>Löschung</Text> Ihrer Daten ("Recht auf Vergessenwerden") (Art. 17 DSGVO).
          {'\n'}• <Text style={{ fontWeight: 'bold' }}>Einschränkung der Verarbeitung</Text> (Art. 18 DSGVO).
          {'\n'}• <Text style={{ fontWeight: 'bold' }}>Datenübertragbarkeit</Text> (Art. 20 DSGVO).
          {'\n'}• <Text style={{ fontWeight: 'bold' }}>Widerruf</Text> einer erteilten Einwilligung (Art. 7 Abs. 3 DSGVO).
          {'\n'}• <Text style={{ fontWeight: 'bold' }}>Beschwerde</Text> bei einer Aufsichtsbehörde (Art. 77 DSGVO).
        </Text>
      </AccordionSection>

      <AccordionSection title="Information über Ihr Widerspruchsrecht nach Art. 21 DSGVO">
          <Text style={styles.bodyText}>
              Sie haben das Recht, aus Gründen, die sich aus Ihrer besonderen Situation ergeben, jederzeit gegen die Verarbeitung Sie betreffender personenbezogener Daten, die aufgrund von Art. 6 Abs. 1 lit. f DSGVO (Datenverarbeitung auf der Grundlage einer Interessenabwägung) erfolgt, Widerspruch einzulegen.
          </Text>
      </AccordionSection>
    </View>
  );
};

export default Step4_YourRights;