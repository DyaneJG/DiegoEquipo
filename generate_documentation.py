from pathlib import Path

root = Path(__file__).resolve().parent
ndoc = root / 'DOCUMENTACION_SISTEMA_RM.md'
lines = [
    '# Documentación del sistema-rm-backend',
    '',
    'Este documento contiene el código fuente completo de las clases desarrolladas en el proyecto, la configuración principal y la explicación de cómo montar el sistema.',
    '',
    '## Resumen del proyecto',
    '',
    '- Aplicación Spring Boot 3.5.16 con Java 21.',
    '- Paquetes principales: auth, catalogo, clientes, notificaciones, pagos, pedidos, repartidores, rutas, tracking, shared.',
    '- Usa PostgreSQL con Flyway, Redis, JWT, Spring Security, Hibernate Spatial y OpenAPI.',
    '- Clase principal: `src/main/java/mx/edomex/rutas/SistemaRmBackendApplication.java`.',
    '',
    '## Configuración del proyecto',
    '',
    '### build.gradle.kts',
    '```kotlin',
]
lines.extend(Path('build.gradle.kts').read_text(encoding='utf-8').splitlines())
lines.append('```')
lines.append('')
lines.append('### application.yml')
lines.append('```yaml')
lines.extend(Path('src/main/resources/application.yml').read_text(encoding='utf-8').splitlines())
lines.append('```')
lines.append('')
lines.append('### Migración inicial de base de datos')
lines.append('```sql')
lines.extend(Path('src/main/resources/db/migration/V1__init_schema.sql').read_text(encoding='utf-8').splitlines())
lines.append('```')
lines.append('')
lines.append('## Clases Java y código fuente')
lines.append('')

files = sorted(root.glob('src/main/java/**/*.java')) + sorted(root.glob('src/test/java/**/*.java'))
for p in files:
    rel = p.relative_to(root).as_posix()
    lines.append(f'## {rel}')
    lines.append('```java')
    lines.extend(p.read_text(encoding='utf-8').splitlines())
    lines.append('```')
    lines.append('')

ndoc.write_text('\n'.join(lines), encoding='utf-8')
print(f'Generated {ndoc}')
