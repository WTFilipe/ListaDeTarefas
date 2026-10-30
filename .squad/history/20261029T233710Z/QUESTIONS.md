# Descoberta do produto

Preciso de contexto para escrever a PRD do app de lista de tarefas. O pedido
original cobre título, descrição, label com cor, busca, ordenação e data limite
com indicador; as perguntas abaixo fecham as lacunas que mais bloqueiam o
escopo.

Responda pelo dashboard (formulário gerado a partir de
`.squad/human/questions.json`).

## 1. Armazenamento (obrigatória)

Como os dados das tarefas devem ser armazenados?

- Somente no aparelho, funcionando offline (padrão sugerido)
- No aparelho, com backup/restauração manual em arquivo
- Na nuvem com login de conta, sincronizando entre aparelhos

## 2. Dispositivos (obrigatória)

Quais dispositivos precisam de suporte?

- Celular Android (padrão sugerido)
- Tablet Android
- Wear OS

## 3. Conclusão de tarefa (obrigatória)

Uma tarefa pode ser marcada como concluída? Se sim, o que acontece com ela na
lista?

- Sim: checkbox risca a tarefa e ela continua na lista, com opção de ocultar
  concluídas (padrão sugerido)
- Sim: checkbox move a tarefa para uma seção/aba "Concluídas"
- Sim: checkbox remove a tarefa da lista ativa e a arquiva
- Não: o app apenas cadastra e edita tarefas no MVP

## 4. Labels (obrigatória)

Como as labels devem funcionar?

- Uma label por tarefa, cor escolhida em uma paleta fixa (padrão sugerido)
- Uma label por tarefa, com seletor de cor livre
- Várias labels por tarefa, cor de paleta fixa
- Várias labels por tarefa, com seletor de cor livre

## 5. Estrutura da lista (obrigatória)

O app terá uma única lista ou várias listas/projetos?

- Uma única lista de tarefas (padrão sugerido)
- Várias listas/projetos, cada um com suas tarefas

## 6. Indicador de prazo (obrigatória)

Quais estados o indicador de proximidade da data limite deve mostrar?

- "Atrasada", "Hoje", "Amanhã" e "Próximos 7 dias" (padrão sugerido)
- "Atrasada", "Hoje" e "Próximos 3 dias"
- Apenas "Atrasada" e "No prazo"

## 7. Lembretes (obrigatória)

O app deve enviar lembretes/notificações da data limite?

- Sem lembretes no MVP; apenas o indicador visual na tarefa (padrão sugerido)
- Notificação no dia da data limite (data, sem hora)
- Notificação em horário configurável pelo usuário (data + hora)

## 8. Ordenação (obrigatória)

Quais opções de ordenação a lista deve oferecer? (pode marcar várias)

- Ordem alfabética do título (A-Z)
- Ordem alfabética do título (Z-A)
- Data de criação (mais recente primeiro)
- Data de criação (mais antiga primeiro)
- Data limite (mais próxima primeiro)
- Data limite (mais distante primeiro)
