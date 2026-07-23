import { createFileRoute } from "@tanstack/react-router";
import AuthForm from "@/components/AuthForm";

export const Route = createFileRoute("/login")({
  head: () => ({
    meta: [
      { title: "Sign in — AutoVault" },
      { name: "description", content: "Sign in to the AutoVault dealership inventory system." },
      { property: "og:title", content: "Sign in — AutoVault" },
      { property: "og:description", content: "Sign in to the AutoVault dealership inventory system." },
    ],
  }),
  component: LoginPage,
});

function LoginPage() {
  return <AuthForm mode="login" />;
}