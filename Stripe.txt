

## Instalación de Stripe CLI

### Prerrequisitos
- Node.js versión 18 o superior
- npm (viene incluido con Node.js)

### Pasos de instalación

## 1. ejecutar el comando en una terminal para la instalación de Stripe CLI
npm install -g @stripe/cli

## 2. iniciar sesión en la pagina de Stripe y solicitar la api secret key

## 3. Ejecutar el siguiente comando en la terminal el cual te dara el webhook_secret_key
stripe listen --forward-to localhost:8080/api/pagos/webhook/stripe

## 4. en el archivo application.yml colocar al final la siguiente configuración, colocando tus keys secretas
stripe:
  secret-key:tu api_secret_key
  webhook-secret: tu_webhook_secret_key
  default-currency: mxn