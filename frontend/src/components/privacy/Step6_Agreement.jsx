import React from 'react';
import { View, Text } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome5';
import BouncyCheckbox from "react-native-bouncy-checkbox";
import { useAuthStore } from '../../store/authStore';
import { getThemeColors } from '../../styles/theme';

const Step6_Agreement = ({ styles, icon, isChecked, onToggleCheck }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);

    return (
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', padding: 8 }}>
            <Icon name={icon} size={48} style={{ alignSelf: 'center', marginBottom: 16, color: styles.title.color }} />
            <Text style={[styles.cardTitle, { textAlign: 'center', borderBottomWidth: 0, paddingBottom: 0 }]}>Zustimmung</Text>
            <Text style={[styles.bodyText, { marginBottom: 16, textAlign: 'center' }]}>
                Sie haben alle Informationen zur Verarbeitung Ihrer Daten erhalten. Bitte bestätigen Sie, dass Sie die Datenschutzerklärung gelesen haben und mit der Verarbeitung Ihrer Daten wie beschrieben einverstanden sind.
            </Text>

            <View style={{ marginTop: 24 }}>
                <BouncyCheckbox
                    text="Ich habe die Datenschutzerklärung gelesen und stimme zu."
                    isChecked={isChecked}
                    onPress={onToggleCheck}
                    textStyle={{ color: colors.text, textDecorationLine: 'none', fontSize: 16 }}
                    fillColor={colors.primary}
                    size={25}
                    innerIconStyle={{ borderWidth: 2 }}
                />
            </View>
        </View>
    );
};

export default Step6_Agreement;