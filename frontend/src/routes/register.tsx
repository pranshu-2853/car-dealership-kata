import { createFileRoute } from "@tanstack/react-router";
import AuthForm from "@/components/AuthForm";

export const Route = createFileRoute("/register")({
  head: () => ({
    meta: [
      { title: "Create account — AutoVault" },
      { name: "description", content: "Create an AutoVault account to browse and purchase vehicles." },
      { property: "og:title", content: "Create account — AutoVault" },
      { property: "og:description", content: "Create an AutoVault account to browse and purchase vehicles." },
    ],
  }),
  component: RegisterPage,
});

function RegisterPage() {
  return <AuthForm mode="register" />;
}