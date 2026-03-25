import clsx from "clsx";
import svgPaths from "./svg-3uos1zq7r3";

function Container6({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0">
      <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex flex-col items-start relative">{children}</div>
    </div>
  );
}

function Container5({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0">
      <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex gap-[16px] items-center relative">{children}</div>
    </div>
  );
}
type Wrapper1Props = {
  additionalClassNames?: string;
};

function Wrapper1({ children, additionalClassNames = "" }: React.PropsWithChildren<Wrapper1Props>) {
  return (
    <div className={clsx("relative shrink-0 w-full", additionalClassNames)}>
      <div aria-hidden="true" className="absolute border-[#374151] border-b border-solid inset-0 pointer-events-none" />
      <div className="flex flex-row items-center min-h-[inherit] size-full">{children}</div>
    </div>
  );
}

function HorizontalBorder2({ children }: React.PropsWithChildren<{}>) {
  return (
    <Wrapper1 additionalClassNames="min-h-[60px]">
      <div className="content-stretch flex items-center justify-between min-h-[inherit] pb-[18.5px] pt-[17.5px] px-[16px] relative w-full">{children}</div>
    </Wrapper1>
  );
}

function HorizontalBorder1({ children }: React.PropsWithChildren<{}>) {
  return (
    <Wrapper1 additionalClassNames="min-h-[60px]">
      <div className="content-stretch flex items-center justify-between min-h-[inherit] pb-[9px] pt-[8px] px-[16px] relative w-full">{children}</div>
    </Wrapper1>
  );
}

function Container4({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="min-h-[72px] relative shrink-0 w-full">
      <div className="flex flex-row items-center min-h-[inherit] size-full">
        <div className="content-stretch flex items-center justify-between min-h-[inherit] px-[16px] py-[12px] relative w-full">{children}</div>
      </div>
    </div>
  );
}

function Container3({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="h-[16px] relative shrink-0 w-[20px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20 16">
        <g id="Container">{children}</g>
      </svg>
    </div>
  );
}

function HorizontalBorder({ children }: React.PropsWithChildren<{}>) {
  return (
    <Wrapper1 additionalClassNames="min-h-[72px]">
      <div className="content-stretch flex items-center justify-between min-h-[inherit] pb-[12.5px] pt-[11.5px] px-[16px] relative w-full">{children}</div>
    </Wrapper1>
  );
}

function Container2({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0 size-[20px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20 20">
        <g id="Container">{children}</g>
      </svg>
    </div>
  );
}

function Wrapper({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0 w-full">
      <div className="content-stretch flex flex-col items-start px-[16px] relative w-full">
        <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold justify-center leading-[0] not-italic relative shrink-0 text-[#9ca3af] text-[14px] tracking-[0.7px] uppercase w-full">
          <p className="leading-[20px]">{children}</p>
        </div>
      </div>
    </div>
  );
}

function Container1() {
  return (
    <div className="h-[12px] relative shrink-0 w-[7.4px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 7.4 12">
        <g id="Container">
          <path d={svgPaths.p28c84800} fill="var(--fill-0, #9CA3AF)" id="Icon" />
        </g>
      </svg>
    </div>
  );
}

function Container() {
  return (
    <div className="h-[7.4px] relative shrink-0 w-[12px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 12 7.4">
        <g id="Container">
          <path d={svgPaths.p1adfde00} fill="var(--fill-0, #9CA3AF)" id="Icon" />
        </g>
      </svg>
    </div>
  );
}
type HeadingTextProps = {
  text: string;
};

function HeadingText({ text }: HeadingTextProps) {
  return <Wrapper>{text}</Wrapper>;
}

export default function GlobalSettingsScreen() {
  return (
    <div className="bg-[#101c22] content-stretch flex flex-col items-start relative size-full" data-name="Global Settings Screen">
      <div className="content-stretch flex flex-col isolate items-start min-h-[1391px] relative shrink-0 w-full" data-name="Container">
        <div className="backdrop-blur-[2px] bg-[rgba(16,28,34,0.8)] relative shrink-0 w-full z-[2]" data-name="Header - Top App Bar">
          <div aria-hidden="true" className="absolute border-[#374151] border-b border-solid inset-0 pointer-events-none" />
          <div className="flex flex-row items-center size-full">
            <div className="content-stretch flex items-center justify-between pb-[17px] pt-[16px] px-[16px] relative w-full">
              <div className="relative shrink-0 size-[40px]" data-name="Container">
                <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex items-center justify-center relative size-full">
                  <div className="relative shrink-0 size-[16px]" data-name="Container">
                    <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 16 16">
                      <g id="Container">
                        <path d={svgPaths.p300a1100} fill="var(--fill-0, #E5E7EB)" id="Icon" />
                      </g>
                    </svg>
                  </div>
                </div>
              </div>
              <div className="flex-[1_0_0] min-h-px min-w-px relative" data-name="Heading 1">
                <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex flex-col items-center relative w-full">
                  <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[28px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[18px] text-center tracking-[-0.45px] w-[69.45px]">
                    <p className="leading-[28px]">Settings</p>
                  </div>
                </div>
              </div>
              <div className="shrink-0 size-[40px]" data-name="Rectangle" />
            </div>
          </div>
        </div>
        <div className="relative shrink-0 w-full z-[1]" data-name="Main">
          <div className="content-stretch flex flex-col gap-[32px] items-start px-[16px] py-[24px] relative w-full">
            <div className="content-stretch flex flex-col gap-[12px] items-start relative shrink-0 w-full" data-name="Appearance Section">
              <HeadingText text="Appearance" />
              <div className="bg-[#18262d] content-stretch flex flex-col items-start overflow-clip relative rounded-[24px] shrink-0 w-full" data-name="Background">
                <HorizontalBorder>
                  <Container5>
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container2>
                        <path d={svgPaths.p1b1d6580} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container2>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[53.08px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[53.08px]">
                          <p className="leading-[24px]">Theme</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#9ca3af] text-[14px] w-[49.11px]">
                          <p className="leading-[21px]">System</p>
                        </div>
                      </div>
                    </div>
                  </Container5>
                  <Container />
                </HorizontalBorder>
                <HorizontalBorder>
                  <Container5>
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container2>
                        <path d={svgPaths.p2ef76100} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container2>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[99.72px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[99.72px]">
                          <p className="leading-[24px]">Accent Color</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#9ca3af] text-[14px] w-[27.5px]">
                          <p className="leading-[21px]">Teal</p>
                        </div>
                      </div>
                    </div>
                  </Container5>
                  <Container />
                </HorizontalBorder>
                <Container4>
                  <div className="content-stretch flex gap-[16px] items-center relative shrink-0" data-name="Container">
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container3>
                        <path d={svgPaths.pbfde080} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container3>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[105.17px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[105.17px]">
                          <p className="leading-[24px]">Channel View</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#9ca3af] text-[14px] w-[23.28px]">
                          <p className="leading-[21px]">List</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  <Container />
                </Container4>
              </div>
            </div>
            <div className="content-stretch flex flex-col gap-[12px] items-start relative shrink-0 w-full" data-name="Playback Section">
              <HeadingText text="Playback" />
              <div className="bg-[#18262d] content-stretch flex flex-col items-start overflow-clip relative rounded-[24px] shrink-0 w-full" data-name="Background">
                <HorizontalBorder>
                  <Container5>
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container2>
                        <path d={svgPaths.p19e3b6c0} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container2>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[107.06px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[107.06px]">
                          <p className="leading-[24px]">Default Player</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#9ca3af] text-[14px] w-[65.16px]">
                          <p className="leading-[21px]">ExoPlayer</p>
                        </div>
                      </div>
                    </div>
                  </Container5>
                  <Container />
                </HorizontalBorder>
                <Container4>
                  <div className="content-stretch flex gap-[16px] items-center relative shrink-0" data-name="Container">
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container3>
                        <path d={svgPaths.pde75900} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container3>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[130.47px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[130.47px]">
                          <p className="leading-[24px]">Preferred Quality</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#9ca3af] text-[14px] w-[30.78px]">
                          <p className="leading-[21px]">Auto</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  <Container />
                </Container4>
              </div>
            </div>
            <div className="content-stretch flex flex-col gap-[12px] items-start relative shrink-0 w-full" data-name="App Behavior Section">
              <HeadingText text="App Behavior" />
              <div className="bg-[#18262d] content-stretch flex flex-col items-start overflow-clip relative rounded-[24px] shrink-0 w-full" data-name="Background">
                <HorizontalBorder>
                  <Container5>
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <div className="h-[15px] relative shrink-0 w-[19px]" data-name="Container">
                        <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 19 15">
                          <g id="Container">
                            <path d={svgPaths.p3eecb600} fill="var(--fill-0, #2BADEE)" id="Icon" />
                          </g>
                        </svg>
                      </div>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[113.97px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[113.97px]">
                          <p className="leading-[24px]">Default Playlist</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#9ca3af] text-[14px] w-[107.31px]">
                          <p className="leading-[21px]">My Main Playlist</p>
                        </div>
                      </div>
                    </div>
                  </Container5>
                  <Container />
                </HorizontalBorder>
                <HorizontalBorder>
                  <Container5>
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container2>
                        <path d={svgPaths.p237be000} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container2>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[75.7px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[75.7px]">
                          <p className="leading-[24px]">Language</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#9ca3af] text-[14px] w-[47.73px]">
                          <p className="leading-[21px]">English</p>
                        </div>
                      </div>
                    </div>
                  </Container5>
                  <Container />
                </HorizontalBorder>
                <Container4>
                  <div className="content-stretch flex gap-[16px] items-center relative shrink-0" data-name="Container">
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <div className="relative shrink-0 size-[18px]" data-name="Container">
                        <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 18 18">
                          <g id="Container">
                            <path d={svgPaths.p315d3200} fill="var(--fill-0, #2BADEE)" id="Icon" />
                          </g>
                        </svg>
                      </div>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[162.86px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[162.86px]">
                          <p className="leading-[24px]">Auto Update Playlists</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#9ca3af] text-[14px] w-[32.69px]">
                          <p className="leading-[21px]">Daily</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                    <div className="content-stretch flex items-start justify-center relative shrink-0 w-[48px]" data-name="Container">
                      <div className="bg-[#374151] flex-[1_0_0] h-[28px] min-h-px min-w-px rounded-[9999px]" data-name="Label" />
                      <div className="absolute bg-[#4b5563] content-stretch flex flex-col items-start p-[4px] right-[-4px] rounded-[9999px] size-[36px] top-[-4px]" data-name="Input">
                        <div aria-hidden="true" className="absolute border-4 border-[rgba(0,0,0,0)] border-solid inset-0 pointer-events-none rounded-[9999px]" />
                        <div className="relative shrink-0 size-[28px]" data-name="image fill">
                          <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex flex-col items-center justify-center overflow-clip relative rounded-[inherit] size-full">
                            <div className="relative shrink-0 size-[28px]" data-name="SVG">
                              <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 28 28">
                                <g id="SVG">
                                  <path d={svgPaths.p312c5b00} fill="var(--fill-0, white)" id="Vector" />
                                </g>
                              </svg>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </Container4>
              </div>
            </div>
            <div className="content-stretch flex flex-col gap-[12px] items-start relative shrink-0 w-full" data-name="Data & Storage Section">
              <Wrapper>{`Data & Storage`}</Wrapper>
              <div className="bg-[#18262d] content-stretch flex flex-col items-start overflow-clip relative rounded-[24px] shrink-0 w-full" data-name="Background">
                <HorizontalBorder1>
                  <Container5>
                    <div className="content-stretch flex items-center justify-center relative shrink-0 size-[48px]" data-name="Container">
                      <div className="h-[22px] relative shrink-0 w-[18px]" data-name="Container">
                        <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 18 22">
                          <g id="Container">
                            <path d={svgPaths.p14f6a6c0} fill="var(--fill-0, #2BADEE)" id="Icon" />
                          </g>
                        </svg>
                      </div>
                    </div>
                    <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                      <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[93.94px]">
                        <p className="leading-[24px]">Clear Cache</p>
                      </div>
                    </div>
                  </Container5>
                  <Container1 />
                </HorizontalBorder1>
                <HorizontalBorder1>
                  <Container5>
                    <div className="content-stretch flex items-center justify-center relative shrink-0 size-[48px]" data-name="Container">
                      <div className="h-[18.35px] relative shrink-0 w-[20px]" data-name="Container">
                        <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20 18.35">
                          <g id="Container">
                            <path d={svgPaths.p279a9400} fill="var(--fill-0, #2BADEE)" id="Icon" />
                          </g>
                        </svg>
                      </div>
                    </div>
                    <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                      <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[115px]">
                        <p className="leading-[24px]">Clear Favorites</p>
                      </div>
                    </div>
                  </Container5>
                  <Container1 />
                </HorizontalBorder1>
                <div className="min-h-[60px] relative shrink-0 w-full" data-name="Container">
                  <div className="flex flex-row items-center min-h-[inherit] size-full">
                    <div className="content-stretch flex items-center justify-between min-h-[inherit] px-[16px] py-[8px] relative w-full">
                      <div className="content-stretch flex gap-[16px] items-center relative shrink-0" data-name="Container">
                        <div className="content-stretch flex items-center justify-center relative shrink-0 size-[48px]" data-name="Container">
                          <div className="h-[18.45px] relative shrink-0 w-[16px]" data-name="Container">
                            <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 16 18.45">
                              <g id="Container">
                                <path d={svgPaths.pbc5000} fill="var(--fill-0, #EF4444)" id="Icon" />
                              </g>
                            </svg>
                          </div>
                        </div>
                        <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                          <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#ef4444] text-[16px] w-[130.14px]">
                            <p className="leading-[24px]">Reset to Defaults</p>
                          </div>
                        </div>
                      </div>
                      <Container1 />
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div className="content-stretch flex flex-col gap-[12px] items-start pb-[32px] relative shrink-0 w-full" data-name="About Section">
              <HeadingText text="About" />
              <div className="bg-[#18262d] content-stretch flex flex-col items-start overflow-clip relative rounded-[24px] shrink-0 w-full" data-name="Background">
                <HorizontalBorder2>
                  <Container6>
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[93.41px]">
                      <p className="leading-[24px]">App Version</p>
                    </div>
                  </Container6>
                  <Container6>
                    <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[20px] justify-center leading-[0] not-italic relative shrink-0 text-[#9ca3af] text-[14px] w-[30.58px]">
                      <p className="leading-[20px]">1.2.3</p>
                    </div>
                  </Container6>
                </HorizontalBorder2>
                <HorizontalBorder2>
                  <Container6>
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[125.89px]">
                      <p className="leading-[24px]">Contact Support</p>
                    </div>
                  </Container6>
                  <Container1 />
                </HorizontalBorder2>
                <div className="min-h-[60px] relative shrink-0 w-full" data-name="Container">
                  <div className="flex flex-row items-center min-h-[inherit] size-full">
                    <div className="content-stretch flex items-center justify-between min-h-[inherit] px-[16px] py-[18px] relative w-full">
                      <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#e5e7eb] text-[16px] w-[107.22px]">
                          <p className="leading-[24px]">Privacy Policy</p>
                        </div>
                      </div>
                      <Container1 />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}