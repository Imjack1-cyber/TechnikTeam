import React from 'react';
import { View, Text, ScrollView, Linking } from 'react-native';
import AccordionSection from '../ui/AccordionSection';
import Icon from 'react-native-vector-icons/FontAwesome5';

const Step3_Recipients = ({ styles, icon }) => {
  const Link = ({ children, href }) => (
    <Text style={styles.link} onPress={() => Linking.openURL(href)}>
      {children}
    </Text>
  );

  return (
    <View style={{ paddingVertical: 24, paddingHorizontal: 8 }}>
      <Icon name={icon} size={48} style={{ alignSelf: 'center', marginBottom: 16, color: styles.title.color }} />
      <Text style={[styles.cardTitle, { textAlign: 'center', borderBottomWidth: 0, paddingBottom: 0 }]}>Empfänger von Daten und Drittlandtransfer</Text>
      <Text style={[styles.bodyText, { marginBottom: 16, textAlign: 'center' }]}>
        Ihre Daten werden mit größter Sorgfalt behandelt und nur wenn absolut notwendig weitergegeben.
      </Text>
      
      <AccordionSection title="Hosting">
        <Text style={styles.bodyText}>
          Die Anwendung und alle Ihre Daten werden auf einem privaten Server gehostet, der sich in Deutschland befindet. Es findet kein direkter Transfer Ihrer Kerndaten an externe Hosting-Anbieter statt.
        </Text>
      </AccordionSection>

      <AccordionSection title="Push-Benachrichtigungen (Google Firebase)">
        <Text style={styles.bodyText}>
          Bei Nutzung unserer mobilen App und Aktivierung von Push-Benachrichtigungen wird ein anonymer Token zur Zustellung dieser Nachrichten an Google Firebase Cloud Messaging (FCM) in den USA übermittelt.
          {'\n\n'}
          Diese Übermittlung ist durch das EU-U.S. Data Privacy Framework legitimiert. Weitere Details finden Sie in der <Link href="https://policies.google.com/privacy">Datenschutzerklärung von Google</Link>.
        </Text>
      </AccordionSection>
    </View>
  );
};

export default Step3_Recipients;