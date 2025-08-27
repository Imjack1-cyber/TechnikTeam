import { useState, useEffect, useRef } from 'react';

const useTypingAnimation = (lines) => {
	const [renderedLines, setRenderedLines] = useState([]);
	const [isComplete, setIsComplete] = useState(false);
	const containerRef = useRef(null);
	const timeoutIds = useRef([]);

	useEffect(() => {
		let isCancelled = false;

		const typeLine = (lineIndex) => {
			if (isCancelled || lineIndex >= lines.length) {
				setIsComplete(true);
				return;
			}

			const currentLine = lines[lineIndex];
			setRenderedLines(prev => {
				const newLines = [...prev];
				newLines[lineIndex] = { ...currentLine, text: '' }; // Initialize line
				return newLines;
			});

			let charIndex = 0;
			const typeChar = () => {
				if (isCancelled) return;

				setRenderedLines(prev => {
					const newLines = [...prev];
					newLines[lineIndex] = {
						...currentLine,
						text: currentLine.text.substring(0, charIndex + 1),
					};
					return newLines;
				});
				
				// In React Native, the component using this hook should attach
				// this ref to a ScrollView and call .scrollToEnd()
				if (containerRef.current?.scrollToEnd) {
					containerRef.current.scrollToEnd({ animated: true });
				}


				if (charIndex < currentLine.text.length - 1) {
					charIndex++;
					const timeoutId = setTimeout(typeChar, currentLine.speed || 30);
					timeoutIds.current.push(timeoutId);
				} else {
					const timeoutId = setTimeout(() => typeLine(lineIndex + 1), currentLine.delayAfter || 300);
					timeoutIds.current.push(timeoutId);
				}
			};
			typeChar();
		};

		typeLine(0);

		return () => {
			isCancelled = true;
			timeoutIds.current.forEach(clearTimeout);
		};
	}, [lines]);

	return { containerRef, renderedLines, isComplete };
};

export default useTypingAnimation;