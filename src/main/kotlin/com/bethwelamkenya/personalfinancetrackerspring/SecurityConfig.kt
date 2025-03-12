package com.bethwelamkenya.personalfinancetrackerspring

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf{it.disable()}  // Disable CSRF protection
//            .authorizeHttpRequests()
            .authorizeHttpRequests { it.anyRequest().permitAll() }
//            .anyRequest().permitAll()  // Allow all requests
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
        return http.build()
    }
//    @Bean
//    @Throws(Exception::class)
//    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
//        http
//            .authorizeHttpRequests { auth ->
//                auth
//                    .requestMatchers("/**").permitAll() // Allow public endpoints
//                    .anyRequest().authenticated()
//            } // Secure other endpoints
//
//            .formLogin { it.disable() }
//            .httpBasic { it.disable() }
//        return http.build()
//    }
//    @Bean
//    fun webSecurityCustomizer(): WebSecurityCustomizer {
//        return WebSecurityCustomizer { web -> web.ignoring().requestMatchers("/users/**") }
//    }
//
//    @Bean
//    fun corsFilter(): CorsFilter {
//        val source = UrlBasedCorsConfigurationSource()
//        val config = CorsConfiguration()
//        config.allowCredentials = true
//        config.addAllowedOrigin("http://localhost:3000")
//        config.addAllowedHeader("*")
//        config.addAllowedMethod("*")
//        source.registerCorsConfiguration("/**", config)
//        return CorsFilter(source)
//    }
}
