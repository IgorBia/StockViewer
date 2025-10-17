import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, CommonModule], // <- to jest kluczowe!
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent {
  loginForm: FormGroup;
  registerForm: FormGroup;
  isLogin = true; // toggle login/register
  error: string = '';

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.loginForm = this.fb.group({
      email: ['', Validators.required],
      password: ['', Validators.required]
    });

    this.registerForm = this.fb.group({
      email: ['', Validators.required],
      password: ['', Validators.required],
      confirmPassword: ['', Validators.required]
    });
  }

  toggleMode() {
    this.isLogin = !this.isLogin;
    this.error = '';
  }

  submitLogin() {
    if (this.loginForm.invalid) return;

    this.auth.login(this.loginForm.value).subscribe({
      next: (res) =>{
        this.auth.saveDTO(res);
        this.router.navigate(['/dashboard'])
      },
      error: err => {
        if (err.error?.fieldErrors?.length) {
          this.error = err.error.fieldErrors.map((e: any) => e.message).join(', ');
        } else {
          this.error = err.error?.message || 'Błąd logowania';
        }
      }
    });
  }

  submitRegister() {
    if (this.registerForm.invalid) return;
    if (this.registerForm.value.password !== this.registerForm.value.confirmPassword) {
      this.error = 'Hasła się nie zgadzają';
      return;
    }

  // Remove confirmPassword from payload
  const { email, password } = this.registerForm.value;
  this.auth.register({ email, password }).subscribe({
      next: () => {
        this.isLogin = true; // przełącz na login po rejestracji
        this.error = 'Rejestracja zakończona, zaloguj się';
        //this.cdr.detectChanges(); // Ręczne wykrywanie zmian
      },
      error: err => {
        if (err.error?.fieldErrors?.length) {
          this.error = err.error.fieldErrors.map((e: any) => e.message).join(', ');
        } else {
          this.error = err.error?.message || 'Błąd rejestracji';
        }
      }
    });
  }
}
