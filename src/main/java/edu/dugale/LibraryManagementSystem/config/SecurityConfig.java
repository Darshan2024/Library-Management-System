package edu.dugale.LibraryManagementSystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import edu.dugale.LibraryManagementSystem.service.JpaUserDetailsService;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/signin", "/css/**", "/","/js/**").permitAll()
              .anyRequest().authenticated()
          )
          .formLogin(form -> form
              .loginPage("/signin")          // serve page
              .loginProcessingUrl("/signin") // process POST here (keeps your path)
              .defaultSuccessUrl("/", true)
              .permitAll()
          )
          .logout(logout -> logout
              .logoutSuccessUrl("/signin?logout")
              .permitAll()
          )
          .csrf(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public DaoAuthenticationProvider authProvider(
            JpaUserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(encoder);
        return p;
    }
}
