import styled from '@emotion/styled';

export const Skeleton = styled.div`
  background-color: ${({ theme }) => theme.headerBorder};
  background: linear-gradient(
      100deg,
      rgba(255, 255, 255, 0) 40%,
      rgba(255, 255, 255, 0.5) 50%,
      rgba(255, 255, 255, 0) 60%
    )
    ${({ theme }) => theme.headerBorder};
  background-size: 200% 100%;
  background-position-x: 180%;
  animation: 1s loading ease-in-out infinite;

  @keyframes loading {
    to {
      background-position-x: -20%;
    }
  }
`;
