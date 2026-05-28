/**
 * Shared source-to-program compilation flow used by RUN and STEP actions.
 */
public final class ProgramCompilationPipeline {

    public enum Status {
        SUCCESS,
        EMPTY_INPUT,
        EMPTY_PROGRAM,
        LEX_ERROR,
        PARSE_ERROR,
        RUNTIME_ERROR
    }

    public static final class Result {
        public final Status status;
        public final Program program;
        public final String errorMessage;

        private Result(Status status, Program program, String errorMessage) {
            this.status = status;
            this.program = program;
            this.errorMessage = errorMessage;
        }

        public static Result success(Program program) {
            return new Result(Status.SUCCESS, program, null);
        }

        public static Result emptyInput() {
            return new Result(Status.EMPTY_INPUT, null, null);
        }

        public static Result emptyProgram() {
            return new Result(Status.EMPTY_PROGRAM, null, null);
        }

        public static Result error(Status status, String errorMessage) {
            return new Result(status, null, errorMessage);
        }
    }

    public Result compile(String source) {
        if (source.trim().isEmpty()) {
            return Result.emptyInput();
        }

        try {
            Program program = new Parser(new Lexer(source).tokenize()).parse();
            if (program.isEmpty()) {
                return Result.emptyProgram();
            }
            return Result.success(program);
        } catch (Lexer.LexError e) {
            return Result.error(Status.LEX_ERROR, e.getMessage());
        } catch (Parser.ParseError e) {
            return Result.error(Status.PARSE_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(Status.RUNTIME_ERROR, e.getMessage());
        }
    }
}