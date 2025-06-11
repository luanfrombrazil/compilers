
public class TokenQueue {

    class TokenCell {

        String token;
        TokenCell next;

        TokenCell(String token) {
            this.token = token;
            this.next = null;
        }
    }

    private TokenCell head;
    private TokenCell tail;

    public TokenQueue() {
        this.head = null;
        this.tail = null;
    }

    public void skipQueue(String token) {
        TokenCell newCell = new TokenCell(token);
        if (head == null) {
            head = tail = newCell;
        } else {
            newCell.next = head;
            head = newCell;
        }
    }

    public void enqueue(String token) {
        TokenCell newCell = new TokenCell(token);
        if (tail == null) {
            head = tail = newCell;
        } else {
            tail.next = newCell;
            tail = newCell;
        }
    }

    public String dequeue() {
        if (head == null) {
            return null;
        }
        String token = head.token;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        return token;
    }

    public String peek() {
        if (head == null) {
            return null;
        }
        return head.token;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public TokenQueue copy() {
        TokenQueue copia = new TokenQueue();
        TokenCell atual = this.head;

        while (atual != null) {
            copia.enqueue(atual.token);
            atual = atual.next;
        }

        return copia;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        TokenCell current = head;
        while (current != null) {
            sb.append(current.token);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }

}
