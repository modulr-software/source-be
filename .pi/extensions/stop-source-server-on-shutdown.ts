import { join } from "node:path";
import type { ExtensionAPI } from "@earendil-works/pi-coding-agent";

/**
 * On pi shutdown, tear down the source-be Clojure backend's tmux session
 * (nREPL JVM + http-kit server) so it doesn't outlive the CLI. Idempotent:
 * `source-server.sh stop` is a no-op if the session doesn't exist.
 */
export default function (pi: ExtensionAPI) {
  pi.on("session_shutdown", async (_event, ctx) => {
    const script = join(ctx.cwd, ".pi/skills/source-server/scripts/source-server.sh");
    try {
      await pi.exec("bash", [script, "stop"], { cwd: ctx.cwd, signal: ctx.signal });
    } catch {
      // Already gone or not in the source-be repo — nothing to do.
    }
  });
}
