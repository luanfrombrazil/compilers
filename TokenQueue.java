public class TokenQueue {    

    private TokenCell head;
    private TokenCell tail;

    public TokenQueue() {
        this.head = null;
        this.tail = null;
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
        if (head == null) return null;
        String token = head.token;
        head = head.next;
        if (head == null) tail = null;
        return token;
    }

    public String peek() {
        if (head == null) return null;
        return head.token;
    }

    public boolean isEmpty() {
        return head == null;
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
