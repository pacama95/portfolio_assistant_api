package com.portfolio.infrastructure.mcp.converter;

import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParameterConversionService Unit Tests")
class ParameterConversionServiceTest {

    @Mock
    private Instance<ParameterConverter> converterInstances;

    @Mock
    private ParameterConverter mockConverter1;

    @Mock
    private ParameterConverter mockConverter2;

    @Mock
    private ParameterConverter mockConverter3;

    private ParameterConversionService parameterConversionService;

    @BeforeEach
    void setUp() {
        // Set up mock converters with required method stubs for constructor logging
        doReturn(String.class).when(mockConverter1).getTargetType();
        doReturn(Integer.class).when(mockConverter2).getTargetType();
        doReturn(Boolean.class).when(mockConverter3).getTargetType();
        
        List<ParameterConverter> converters = List.of(mockConverter1, mockConverter2, mockConverter3);
        when(converterInstances.stream()).thenReturn(converters.stream());
        
        parameterConversionService = new ParameterConversionService(converterInstances);
    }

    @Test
    @DisplayName("Should return null when input value is null")
    void shouldReturnNullWhenInputValueIsNull() {
        // When
        Object result = parameterConversionService.convert(null, "anyParameter");

        // Then
        assertNull(result);
        
        // Verify no converters were called since input was null
        verify(mockConverter1, never()).matches(anyString());
        verify(mockConverter2, never()).matches(anyString());
        verify(mockConverter3, never()).matches(anyString());
    }

    @Test
    @DisplayName("Should select first matching converter and delegate conversion")
    void shouldSelectFirstMatchingConverterAndDelegateConversion() {
        // Given
        Object inputValue = "test-value";
        Object expectedResult = "converted-value";
        String parameterName = "testParameter";

        when(mockConverter1.matches(parameterName)).thenReturn(false);
        when(mockConverter2.matches(parameterName)).thenReturn(true);
        when(mockConverter2.convert(inputValue, parameterName)).thenReturn(expectedResult);

        // When
        Object result = parameterConversionService.convert(inputValue, parameterName);

        // Then
        assertSame(expectedResult, result);
        
        // Verify the service checked converters in order and stopped at first match
        verify(mockConverter1).matches(parameterName);
        verify(mockConverter2).matches(parameterName);
        verify(mockConverter3, never()).matches(parameterName); // Should not check third since second matched
        
        // Verify conversion was delegated to the matching converter
        verify(mockConverter2).convert(inputValue, parameterName);
        verify(mockConverter1, never()).convert(any(), anyString());
        verify(mockConverter3, never()).convert(any(), anyString());
    }

    @Test
    @DisplayName("Should return original value when no converter matches")
    void shouldReturnOriginalValueWhenNoConverterMatches() {
        // Given
        Object inputValue = "unmatched-value";
        String parameterName = "unmatchedParameter";

        when(mockConverter1.matches(parameterName)).thenReturn(false);
        when(mockConverter2.matches(parameterName)).thenReturn(false);
        when(mockConverter3.matches(parameterName)).thenReturn(false);

        // When
        Object result = parameterConversionService.convert(inputValue, parameterName);

        // Then
        assertSame(inputValue, result);
        
        // Verify all converters were checked
        verify(mockConverter1).matches(parameterName);
        verify(mockConverter2).matches(parameterName);
        verify(mockConverter3).matches(parameterName);
        
        // Verify no conversion was attempted
        verify(mockConverter1, never()).convert(any(), anyString());
        verify(mockConverter2, never()).convert(any(), anyString());
        verify(mockConverter3, never()).convert(any(), anyString());
    }

    @Test
    @DisplayName("Should stop at first matching converter even if later converters would also match")
    void shouldStopAtFirstMatchingConverter() {
        // Given
        Object inputValue = "test-value";
        Object expectedResult = "first-converter-result";
        String parameterName = "testParameter";

        when(mockConverter1.matches(parameterName)).thenReturn(true);
        when(mockConverter1.convert(inputValue, parameterName)).thenReturn(expectedResult);
        // Note: Not stubbing mockConverter2 and mockConverter3.matches() since they shouldn't be called

        // When
        Object result = parameterConversionService.convert(inputValue, parameterName);

        // Then
        assertSame(expectedResult, result);
        
        // Verify only first converter was checked and used
        verify(mockConverter1).matches(parameterName);
        verify(mockConverter1).convert(inputValue, parameterName);
        
        // Verify later converters were never checked
        verify(mockConverter2, never()).matches(parameterName);
        verify(mockConverter3, never()).matches(parameterName);
        verify(mockConverter2, never()).convert(any(), anyString());
        verify(mockConverter3, never()).convert(any(), anyString());
    }

    @Test
    @DisplayName("Should handle empty converter list gracefully")
    void shouldHandleEmptyConverterListGracefully() {
        // Given
        when(converterInstances.stream()).thenReturn(List.<ParameterConverter>of().stream());
        ParameterConversionService emptyService = new ParameterConversionService(converterInstances);
        Object inputValue = "test-value";

        // When
        Object result = emptyService.convert(inputValue, "anyParameter");

        // Then
        assertSame(inputValue, result);
    }

    @Test
    @DisplayName("Should handle different parameter names independently")
    void shouldHandleDifferentParameterNamesIndependently() {
        // Given
        Object inputValue = "test-value";
        
        // Set up stubs for param1 scenario
        when(mockConverter1.matches("param1")).thenReturn(true);
        when(mockConverter1.convert(inputValue, "param1")).thenReturn("result1");
        
        // Set up stubs for param2 scenario
        when(mockConverter1.matches("param2")).thenReturn(false);
        when(mockConverter2.matches("param2")).thenReturn(true);
        when(mockConverter2.convert(inputValue, "param2")).thenReturn("result2");

        // When
        Object result1 = parameterConversionService.convert(inputValue, "param1");
        Object result2 = parameterConversionService.convert(inputValue, "param2");

        // Then
        assertEquals("result1", result1);
        assertEquals("result2", result2);
        
        // Verify correct converters were used for each parameter
        verify(mockConverter1).convert(inputValue, "param1");
        verify(mockConverter2).convert(inputValue, "param2");
    }
}
