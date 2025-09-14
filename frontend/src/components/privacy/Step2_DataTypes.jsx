import React from 'react';
import { View, Text, ScrollView, Linking } from 'react-native';
import AccordionSection from '../ui/AccordionSection';
import Icon from 'react-native-vector-icons/FontAwesome5';

const Step2_DataTypes = ({ styles, icon }) => {
  const Link = ({ children, href }) => (
    <Text style={styles.link} onPress={() => Linking.openURL(href)}>
      {children}
    </Text>
  );

  return (
    <View style={{ paddingVertical: 24, paddingHorizontal: 8 }}>
      <Icon name={icon} size={48} style={{ alignSelf: 'center', marginBottom: 16, color: styles.title.color }} />
      <Text style={[styles.cardTitle, { textAlign: 'center', borderBottomWidth: 0, paddingBottom: 0 }]}>Art, Zweck und Rechtsgrundlage der verarbeiteten Daten</Text>
      <Text style={[styles.bodyText, { marginBottom: 16, textAlign: 'center' }]}>
        Wir verarbeiten personenbezogene Daten unserer Nutzer grundsätzlich nur, soweit dies zur Bereitstellung einer funktionsfähigen Anwendung sowie unserer Inhalte und Leistungen erforderlich ist.
      </Text>

      <AccordionSection title="a) Bereitstellung der Anwendung und Erstellung von Logfiles">
        <Text style={styles.bodyText}>
          • <Text style={{ fontWeight: 'bold' }}>Verarbeitete Daten:</Text> IP-Adresse, Datum und Uhrzeit des Zugriffs, User-Agent-String (Browser-Typ und -Version, Betriebssystem).{'\n'}
          • <Text style={{ fontWeight: 'bold' }}>Zweck:</Text> Die vorübergehende Speicherung der IP-Adresse ist notwendig, um eine Auslieferung der Anwendung an den Rechner des Nutzers zu ermöglichen. Die Speicherung in Logfiles erfolgt, um die Funktionsfähigkeit der Anwendung sicherzustellen, die Sicherheit unserer informationstechnischen Systeme zu gewährleisten und zur Missbrauchserkennung (z.B. Abwehr von Brute-Force-Angriffen).{'\n'}
          • <Text style={{ fontWeight: 'bold' }}>Rechtsgrundlage:</Text> Die Verarbeitung dieser Daten erfolgt auf Grundlage von Art. 6 Abs. 1 lit. f DSGVO. Unser berechtigtes Interesse liegt in der Gewährleistung der Stabilität und Sicherheit unserer privat betriebenen Systeme.{'\n'}
          • <Text style={{ fontWeight: 'bold' }}>Speicherdauer:</Text> Sicherheitsrelevante Logfiles werden in der Regel für <Text style={{fontWeight: 'bold'}}>6 Monate</Text> gespeichert.
        </Text>
      </AccordionSection>
      <AccordionSection title="b) Registrierung und Verwaltung der Mitgliedschaft">
        <Text style={styles.bodyText}>
            • <Text style={{fontWeight: 'bold'}}>Verarbeitete Daten:</Text> Benutzername, gehashtes Passwort, E-Mail-Adresse, Vor- und Nachname (sofern im Benutzernamen enthalten oder als separates Feld erfasst), Klasse/Jahrgangsstufe.{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Zweck:</Text> Diese Daten sind für die Erstellung und Verwaltung Ihres Benutzerkontos, zur Authentifizierung (Login) und zur organisatorischen Zuordnung innerhalb der Technik-AG unerlässlich.{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Rechtsgrundlage:</Text> Die Verarbeitung erfolgt zur Durchführung des Mitgliedschaftsverhältnisses in der Technik-AG auf Grundlage von Art. 6 Abs. 1 lit. b DSGVO.{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Speicherdauer:</Text> Diese Stammdaten werden für die Dauer Ihrer aktiven Mitgliedschaft in der Technik-AG gespeichert.
        </Text>
      </AccordionSection>
      <AccordionSection title="c) Sitzungsverwaltung (Session-Token)">
        <Text style={styles.bodyText}>
            • <Text style={{fontWeight: 'bold'}}>Verarbeitete Daten:</Text> Ein kryptografisch signiertes, pseudonymes Token (JWT).{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Zweck:</Text> Dieses Token ist technisch zwingend erforderlich, um Sie während Ihrer Sitzung angemedet zu halten.{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Rechtsgrundlage:</Text> § 25 Abs. 2 Nr. 2 TTDSG und Art. 6 Abs. 1 lit. b DSGVO.{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Speicherdauer:</Text> 8 Stunden (Web) / 14 Tage (mobil).
        </Text>
      </AccordionSection>
      <AccordionSection title="d) Nutzung der Kernfunktionen der Anwendung">
        <Text style={styles.bodyText}>
            • <Text style={{fontWeight: 'bold'}}>Verarbeitete Daten:</Text> Qualifikationen, Event-Teilnahmen, Aufgaben, Chat-Nachrichten, Feedback, UI-Einstellungen.{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Zweck:</Text> Erfüllung der Aufgaben der Technik-AG.{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Rechtsgrundlage:</Text> Art. 6 Abs. 1 lit. b DSGVO.{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Speicherdauer:</Text> Für die Dauer Ihrer Mitgliedschaft.
        </Text>
      </AccordionSection>
      <AccordionSection title="e) Sicherheitsfunktionen (2FA und GeoIP)">
        <Text style={styles.bodyText}>
            • <Text style={{fontWeight: 'bold'}}>Verarbeitete Daten:</Text> IP-Adresse, Ländercode.{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Zweck:</Text> Erhöhung der Kontosicherheit.{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Rechtsgrundlage:</Text> Art. 6 Abs. 1 lit. f DSGVO.{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Datenübermittlung:</Text> Keine. Abgleich erfolgt mit einer lokalen Datenbank.
        </Text>
      </AccordionSection>
      <AccordionSection title="f) Push-Benachrichtigungen (Mobile App)">
        <Text style={styles.bodyText}>
            • <Text style={{fontWeight: 'bold'}}>Verarbeitete Daten:</Text> Pseudonymer Push-Benachrichtigungs-Token (FCM-Token).{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Zweck:</Text> Zustellung von Benachrichtigungen.{'\n'}
            • <Text style={{fontWeight: 'bold'}}>Rechtsgrundlage:</Text> Ihre Einwilligung (Art. 6 Abs. 1 lit. a DSGVO).
        </Text>
      </AccordionSection>
    </View>
  );
};

export default Step2_DataTypes;