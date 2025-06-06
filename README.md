# ⏱️ Sistema Servidor de Tempo

Este sistema implementa uma aplicação cliente-servidor em Java para **sincronização de tempo** entre múltiplos clientes e um servidor central, levando em conta os tempos de ida e volta das mensagens. O sistema registra logs das ações de cada cliente, e os clientes podem definir o intervalo entre atualizações.

---

## 📋 Descrição do Sistema

O sistema é composto por duas partes:

- **Servidor de Tempo**:
  - Aguarda conexões de múltiplos clientes via socket.
  - Fornece a hora atual considerando o tempo de ida e volta da comunicação.
  - Mantém um log detalhado das interações de cada cliente.
  - Gerencia os clientes conectados e os respectivos intervalos de atualização.

- **Cliente**:
  - Conecta-se ao servidor.
  - Solicita a hora em um intervalo definido pelo próprio usuário.
  - Calcula o tempo estimado de resposta com base no tempo de envio e recebimento.
  - Atualiza seu relógio com base no tempo do servidor ajustado.

---

## 🕹 Como Executar

### 1. Compile os arquivos Java:
