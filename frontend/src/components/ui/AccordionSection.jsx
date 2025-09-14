import React, { useState } from 'react';
import { View, Text, TouchableOpacity, LayoutAnimation, Platform, UIManager, StyleSheet } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { getThemeColors, spacing } from '../../styles/theme';
import { useAuthStore } from '../../store/authStore';

// Enable layout animation on Android
if (Platform.OS === 'android' && UIManager.setLayoutAnimationEnabledExperimental) {
  UIManager.setLayoutAnimationEnabledExperimental(true);
}

const AccordionSection = ({ title, children }) => {
  const [expanded, setExpanded] = useState(false);
  const theme = useAuthStore(state => state.theme);
  const colors = getThemeColors(theme);
  const accordionStyles = styles(theme);

  const toggleExpand = () => {
    LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);
    setExpanded(!expanded);
  };

  return (
    <View style={accordionStyles.container}>
      <TouchableOpacity style={accordionStyles.header} onPress={toggleExpand} activeOpacity={0.7}>
        <Text style={accordionStyles.title}>{title}</Text>
        <Icon name={expanded ? "chevron-up" : "chevron-down"} size={20} color={colors.textMuted} />
      </TouchableOpacity>
      {expanded && <View style={accordionStyles.content}>{children}</View>}
    </View>
  );
};

const styles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: {
            marginBottom: 12,
            borderRadius: 8,
            borderWidth: 1,
            borderColor: colors.border,
            overflow: 'hidden',
        },
        header: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
            paddingVertical: 12,
            paddingHorizontal: 16,
            backgroundColor: colors.background,
        },
        title: {
            fontSize: 16,
            fontWeight: '600',
            color: colors.text,
            flex: 1, // Allow text to wrap
        },
        content: {
            padding: 16,
            backgroundColor: colors.surface,
        },
    });
};

export default AccordionSection;