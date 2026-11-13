# 📅 WeeklySync — Gestor de Rotina, Estudos e Hábitos

O **WeeklySync** é um aplicativo nativo para Android desenvolvido em **Java** com foco na organização semanal de tarefas, acompanhamento diário de hábitos saudáveis (exercícios físicos e leitura) e análise gráfica de desempenho mensal com histórico contínuo.

## ✨ Funcionalidades Principais

* **Autenticação e Segurança de Dados:**
  * Cadastro e login de usuários com persistência local em banco de dados relacional.
  * Criptografia de senhas utilizando algoritmo de hash **SHA-256** antes da gravação no banco.
  * Validação de força de senha via Expressões Regulares (mínimo de 8 caracteres, exigindo números ou símbolos).
  * Verificação de e-mail em duas camadas: validação sintática (`Patterns.EMAIL_ADDRESS`) e consulta real de DNS na rede (`InetAddress`) para comprovar a existência do domínio informado.

* **Gestão de Rotina Semanal (CRUD):**
  * Criação, listagem, conclusão (com efeito visual *strikethrough*) e exclusão de tarefas.
  * Isolamento de dados por sessão de usuário.
  * Organização por dias da semana através de seletores dinâmicos (`Spinner` e `RecyclerView`).

